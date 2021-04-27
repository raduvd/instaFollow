package ro.personal.home.instafollow.service;


import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.webDriver.model.Result;
import ro.personal.home.instafollow.webDriver.webDriver.AppWebDriver;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

@Data
@Component
@Scope("singleton")
public class PageService {

    public static final By ACCEPT_ALL_COOKIES_BUTTON = By.xpath("//button[text()='Accept All']");
    public static final By LOGIN_BUTTON = By.xpath("//a[text()='Log In']");
    public static final By USERNAME_INPUT = By.xpath("//input[@name='username']");
    public static final By PASSWORD_INPUT = By.xpath("//input[@name='password']");
    public static final By SEARCH_INPUT = By.xpath("//input[@placeholder='Search']");
    public static final By LOGIN_BUTTON_ON_CREDENTIAL_PAGE = By.xpath("//div[text()='Log In']");
    public static final By NOT_NOW_BUTTON = By.xpath("//button[text()='Not Now']");
    public static final By UP_TO_THE_ENTIRE_LIST = By.xpath("//div[@role='dialog']/div/div[2]/ul/div/li");

    private WebDriver webDriver;

    private Result result;

    private CookieService cookieService;

    private AccountService accountService;

    @Autowired
    public PageService(CookieService cookieService, AccountService accountService) {
        this.cookieService = cookieService;
        this.accountService = accountService;
    }

    /**
     * Opens pageAddress in webdriver. Logs in user manually or by loading cookies, if present.
     * This is a mandatory first step before going to other pages.
     */
    public void initializePage(PageAddress pageAddress, String userName, Boolean useCookiesToLogin) {

        this.result = new Result();
        this.webDriver = AppWebDriver.getWebDriver();

        Pair<String, String> account = accountService.getAccount(userName);

        goToPage(PageAddress.INSTAGRAM_RAW.getLinkToPage());
        cookieService.loadCookies(userName);
        goToPage(pageAddress.getLinkToPage());

        if (!useCookiesToLogin) {
            //Go to main login page
            goToPage(PageAddress.INSTAGRAM_RAW.getLinkToPage());
            loginAndHandlePopUps(account);
            cookieService.saveCookies(userName);
            //Go to initial page
            goToPage(pageAddress.getLinkToPage());
        }
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

    public void goToPage(String pageAddress) {
        System.out.println("------------------------------------ We will try to open the following link: " + pageAddress);
        webDriver.get(pageAddress);
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
