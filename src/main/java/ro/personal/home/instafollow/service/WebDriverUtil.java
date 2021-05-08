package ro.personal.home.instafollow.service;

import org.openqa.selenium.By;
import ro.personal.home.instafollow.enums.PageAddress;

public class WebDriverUtil {

    public static final By PAGE_PICTURES = By.xpath("//*[@alt and @srcset]/../../..");
    public static final By CLOSE = By.xpath("//*[@aria-label='Close']");
    public static final By UNFOLLOW_CONFIRMATION = By.xpath("//button[text() = 'Unfollow']");
    public static final By PRIVATE_ACCOUNT_TEXT = By.xpath("//*[text()='The Account is Private']");

    public static String createPageAddress(String relativeUrl) {
        return PageAddress.INSTAGRAM_RAW.getLinkToPage() + relativeUrl;
    }
}
