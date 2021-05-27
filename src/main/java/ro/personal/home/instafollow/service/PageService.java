package ro.personal.home.instafollow.service;


import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.AppWebDriver;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@Component
@Scope("singleton")
public class PageService {

    Logger logger = LoggerFactory.getLogger(PageService.class);

    public static final By ACCEPT_ALL_COOKIES_BUTTON = By.xpath("//button[text()='Accept All']");
    public static final By LOGIN_BUTTON = By.xpath("//a[text()='Log In']");
    public static final By USERNAME_INPUT = By.xpath("//input[@name='username']");
    public static final By PASSWORD_INPUT = By.xpath("//input[@name='password']");
    public static final By SEARCH_INPUT = By.xpath("//input[@placeholder='Search']");
    public static final By LOGIN_BUTTON_ON_CREDENTIAL_PAGE = By.xpath("//div[text()='Log In']");
    public static final By NOT_NOW_BUTTON = By.xpath("//button[text()='Not Now']");
    public static final By UP_TO_THE_ENTIRE_LIST = By.xpath("//div[@role='dialog']/div/div[2]/ul/div/li");

    private WebDriver webDriver;

    private CookieService cookieService;

    private AccountService accountService;

    private PotentialFollowersJpaRepository potentialFollowersJpaRepository;

    @Autowired
    public PageService(CookieService cookieService, AccountService accountService, PotentialFollowersJpaRepository potentialFollowersJpaRepository) {
        this.cookieService = cookieService;
        this.accountService = accountService;
        this.potentialFollowersJpaRepository = potentialFollowersJpaRepository;
    }

    /**
     * Opens pageAddress in webdriver. Logs in user manually or by loading cookies, if present.
     * This is a mandatory first step before going to other pages.
     */
    public void initializePage(PageAddress pageAddress, String userName, Boolean useCookiesToLogin) {

        this.webDriver = AppWebDriver.getWebDriver();

        Pair<String, String> account = accountService.getAccount(userName);

        goToPage(false, PageAddress.INSTAGRAM_RAW.getLinkToPage());
        cookieService.loadCookies(userName);
        goToPage(false, pageAddress.getLinkToPage());

        if (!useCookiesToLogin) manualLogin(pageAddress, userName);
    }

    /**
     * Basically loads the login cookies from another user.
     * Mandatory used after the initialize method.
     * Method is designed to change the user of an already initialized webdriver.
     */
    public void changeUser(PageAddress pageAddress, String userName, Boolean useCookiesToLogin) {

        logger.info("SWITCHING USER TO: " + userName);
        cookieService.loadCookies(userName);
        goToPage(false, pageAddress.getLinkToPage());

        if (!useCookiesToLogin) manualLogin(pageAddress, userName);
    }

    public WebElement findSubElementBy(Boolean continueOnError, WebElement parentElement, By... locators) {

        int locatorsSize = locators.length;

        for (int i = 0; i < locatorsSize; i++) {
            try {
                logger.info("Trying to find element by {}" , locators[i].toString());
                WebElement element = parentElement.findElement(locators[i]);
                logger.info("Returning element found by: {}", locators[i].toString());
                return element;

            } catch (Exception e) {
                logger.info("Element was not found BY: {}", locators[i].toString());
                if (i + 1 >= locatorsSize && !continueOnError)
                    throw new RuntimeException("The element was not found using the following locatos: " +
                            Arrays.stream(locators).map(by -> by.toString()).collect(Collectors.toList()));
            }
        }
        return null;
    }

    private void manualLogin(PageAddress pageAddress, String userName) {

        Pair<String, String> account = accountService.getAccount(userName);
        //Go to main login page
        goToPage(false, PageAddress.INSTAGRAM_RAW.getLinkToPage());
        loginAndHandlePopUps(account);
        cookieService.saveCookies(userName);
        //Go to initial page
        goToPage(false, pageAddress.getLinkToPage());
    }

    private void loginAndHandlePopUps(Pair<String, String> account) {

        waitForButtonAndClickIt(false, ACCEPT_ALL_COOKIES_BUTTON);
        logIn(account);
        waitForButtonAndClickIt(false, LOGIN_BUTTON_ON_CREDENTIAL_PAGE);
        waitForButtonAndClickIt(false, NOT_NOW_BUTTON);
    }

    private void logIn(Pair<String, String> account) {

        waitForInputAndEnterText(USERNAME_INPUT, account.getFirst());
        waitForInputAndEnterText(PASSWORD_INPUT, account.getSecond());
    }

    public void sendKeyDownToElement(WebElement webElement) {
        Actions actions = new Actions(webDriver);
        actions.moveToElement(webElement).sendKeys(Keys.DOWN).perform();
    }

    public void moveToElement(WebElement webElement) {
        if (webElement != null) {
            Actions actions = new Actions(webDriver);
            actions.moveToElement(webElement).perform();
        }
    }

    public void waitForElementAndMoveToIt(Boolean continueOnError, By... locators) {

        moveToElement(WaitDriver.waitAndGetElement(continueOnError, locators));
    }

    public void clickByMovingOnElement(WebElement element) {

        Actions actions = new Actions(webDriver);
        actions.moveToElement(element).click().perform();
    }

    public boolean goToPage(Boolean wait, String pageAddress) {
        int timeToWait = wait ? 25000 : 0;
        logger.info("------------------------------------ We will try to open the following link: " + pageAddress +
                " after waiting milliseconds: {}" ,timeToWait);
        WaitDriver.sleepForMiliseconds(timeToWait);
        webDriver.get(pageAddress);

        if (wait && !isPageAvailable()) {
            logger.info("The page ({}) was unavailable.",  pageAddress);
            return false;
        }
        return true;
    }

    public boolean goToPotentialFollowerPage(PotentialFollower potentialFollower) {
        String pageAddress = WebDriverUtil.createPageAddress(potentialFollower.getId());
        if (!goToPage(true, pageAddress)) {
            potentialFollower.setPageCanBeOpened(false);
            potentialFollowersJpaRepository.saveAndFlush(potentialFollower);
            return false;
        }
        return true;
    }

    //There are several reasons why I cannot go to the page:
    //  *The user has blocked me (this happens a lot actually).
    //  *Instagram has blocked me.
    //  *The page does not exist, simply the account was erased.
    //  *There is an error in the URL, so better not mess with these.
    //TODO pages that do not oppen eg: https://www.instagram.com/bradcalin https://www.instagram.com/jasminka_2021
    //          and the follow request is not sent, just remove them from DB, if the follow request is already sent,
    //          this means that the page opend once so just try again
    //           or maybe I just need a backslash? try this before going deeper
    //          DO NOT just leave it like this because it seems that is NOT something from instagram,
    //          every time it does not work

    private static final By PAGE_UNAVAILABLE = By.xpath("//a[text()='Go back to Instagram.']");

    private boolean isPageAvailable() {
        return WaitDriver.waitForElement(true, PAGE_UNAVAILABLE) == null;
    }

    public void waitForButtonAndClickIt(Boolean continueOnError, By... locator) {

        clickByMovingOnElement(WaitDriver.waitAndGetElement(false, locator));
    }

    private void waitForInputAndEnterText(By locator, String textToInput) {
        WebElement element = WaitDriver.waitAndGetElement(false, locator);

        assert element != null;
        element.click();
        element.sendKeys(textToInput);
    }

    public Object getValueFromElement(Boolean continueOnError, ElementValue elementValue, By... locators) {

        WebElement element = WaitDriver.waitAndGetElement(continueOnError, locators);
        if (element == null) throw new RuntimeException("The element here should never be null. Element with locator.");

        String stringValue = element.getText();

        if (stringValue == null || stringValue.isEmpty())
            throw new RuntimeException("The element here should never be null. Element with locator.");

        switch (elementValue) {
            case NUMBER_WITH_K_COMA_OR_POINT:
                stringValue = stringValue.replace("k", "000");
                stringValue = stringValue.replace(",", "");
                stringValue = stringValue.replace(".", "");
                return Integer.valueOf(stringValue);
            case TEXT:
            default:
                return stringValue;
        }
    }
}
