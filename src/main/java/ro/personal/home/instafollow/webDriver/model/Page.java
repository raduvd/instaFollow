package ro.personal.home.instafollow.webDriver.model;


import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.data.util.Pair;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.webDriver.webDriver.AppWebDriver;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.StringTokenizer;

@Data
public class Page {

    public static final By ACCEPT_ALL_COOKIES_BUTTON = By.xpath("//button[text()='Accept All']");
    public static final By LOGIN_BUTTON = By.xpath("//a[text()='Log In']");
    public static final By USERNAME_INPUT = By.xpath("//input[@name='username']");
    public static final By PASSWORD_INPUT = By.xpath("//input[@name='password']");
    public static final By SEARCH_INPUT = By.xpath("//input[@placeholder='Search']");
    public static final By LOGIN_BUTTON_ON_CREDENTIAL_PAGE = By.xpath("//div[text()='Log In']");
    public static final By NOT_NOW_BUTTON = By.xpath("//button[text()='Not Now']");
    public static final By UP_TO_THE_ENTIRE_LIST = By.xpath("//div[@role='dialog']/div/div[2]/ul/div/li");
    private org.openqa.selenium.WebDriver webDriver;
    private Result result;

    /**
     * Provides a Page, log-in and cookies accepted.
     * This is a mandatory first step before going to other pages.
     *
     * @param pageAddress
     */
    public Page(PageAddress pageAddress, Pair<String, String> account) {

        this.result = new Result();
        this.webDriver = AppWebDriver.getWebDriver();

        //TODO Review IG logic and remove deprecated
        //TODO test the if block and also withouth the if block
        //TODO put cookies into CookieService corresponding to Cookie JPA entity
        //TODO break Page once more?
        //TODO check warnings in all the classes that I have created and Page also


        loadCookies();
        goToPage(pageAddress.getLinkToPage());

        if (isLoginStillPresent()) {
            //Go to main login page
            goToPage(PageAddress.INSTAGRAM_RAW.getLinkToPage());
            loginAndHandlePopUps(pageAddress, account);
            saveCookies();
            //Go to initial page
            goToPage(pageAddress.getLinkToPage());
        }
    }

    private boolean isLoginStillPresent() {
        if (WaitDriver.waitAndGetElement(true, ACCEPT_ALL_COOKIES_BUTTON) == null) {
            return false;
        }
        return false;

    }

    private void loginAndHandlePopUps(PageAddress pageAddress, Pair<String, String> account) {

        waitForButtonAndClickIt(ACCEPT_ALL_COOKIES_BUTTON);
        logIn(account);
        waitForButtonAndClickIt(LOGIN_BUTTON_ON_CREDENTIAL_PAGE);
        waitForButtonAndClickIt(NOT_NOW_BUTTON);
    }

    private void saveCookies() {

        // create file named Cookies to store Login Information
        File file = new File("CookiesIG.data");
        try {
            // Delete old file if exists
            file.delete();
            file.createNewFile();
            FileWriter fileWrite = new FileWriter(file);
            BufferedWriter Bwrite = new BufferedWriter(fileWrite);

            // loop for getting the cookie information
            for (Cookie ck : webDriver.manage().getCookies()) {
                Bwrite.write((ck.getName() + ";" + ck.getValue() + ";" + ck.getDomain() +
                        ";" + ck.getPath() + ";" + ck.getExpiry() + ";" + ck.isSecure()));
                Bwrite.newLine();
            }
            Bwrite.close();
            fileWrite.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadCookies() {

        goToPage(PageAddress.INSTAGRAM_RAW.getLinkToPage());

        try {
            File file = new File("CookiesIG.data");
            FileReader fileReader = new FileReader(file);
            BufferedReader Buffreader = new BufferedReader(fileReader);
            String strline;
            while ((strline = Buffreader.readLine()) != null) {
                StringTokenizer token = new StringTokenizer(strline, ";");
                while (token.hasMoreTokens()) {
                    String name = token.nextToken();
                    String value = token.nextToken();
                    String domain = token.nextToken();
                    String path = token.nextToken();
                    Date expiry = null;

                    String val;
                    if (!(val = token.nextToken()).equals("null")) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");
                        expiry = java.sql.Date.valueOf(LocalDate.parse(val, formatter));
                    }
                    Boolean isSecure = Boolean.valueOf(token.nextToken()).
                            booleanValue();
                    Cookie ck = new Cookie(name, value, domain, path, expiry, isSecure);
                    System.out.println(ck);
                    getWebDriver().manage().addCookie(ck); // This will add the stored cookie to your current session
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void logIn(Pair<String, String> account) {

        waitForInputAndEnterText(USERNAME_INPUT, account.getFirst());
        waitForInputAndEnterText(PASSWORD_INPUT, account.getSecond());
    }

    public void moveCursorToElementAndScroll(WebElement webElement) {
        moveToElement(webElement);
        //((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        //((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,500)");
        webElement.sendKeys(Keys.DOWN);
    }

    public void moveToElement(WebElement webElement) {
        if (webElement != null) {
            Actions actions = new Actions(webDriver);
            actions.moveToElement(webElement).click().perform();
        }
    }

    public void clickByMovingOnElement(WebElement element) {

        Actions actions = new Actions(webDriver);
        actions.moveToElement(element).click().perform();
    }

    public void goToPage(String pageAddress) {
        System.out.println("------------------------------------ We will try to open the following link: " + pageAddress);
        webDriver.get(pageAddress);
    }

    public void waitForButtonAndClickIt(By... locator) {

        clickByMovingOnElement(WaitDriver.waitAndGetElement(false, locator));
    }

    private void waitForInputAndEnterText(By locator, String textToInput) {
        WebElement element = WaitDriver.waitAndGetElement(false, locator);

        element.click();
        element.sendKeys(textToInput);
    }

    public Object getValueFromElement(Boolean continueOnError, ElementValue elementValue, By... locators) {

        WebElement element = WaitDriver.waitAndGetElement(continueOnError, locators);

        String stringValue = element.getText();

        if (stringValue == null || stringValue.isEmpty())
            throw new RuntimeException("The value found by locators: " + locators + " is null or empthy, meaning that something is not correct in my code.");

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
