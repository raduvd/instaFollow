package ro.personal.home.instafollow.service;

import org.openqa.selenium.By;
import ro.personal.home.instafollow.enums.PageAddress;

public class WebDriverUtil {

    public static final By PAGE_PICTURES = By.xpath("//article/div/div/div/div/a");
    public static final By CLOSE = By.xpath("//*[@aria-label='Close']");
    public static final By UNFOLLOW_CONFIRMATION = By.xpath("//button[text() = 'Unfollow']");
    public static final By THE_ACCOUNT_IS_PRIVATE = By.xpath("//*[text()='The Account is Private']");
    public static final By THIS_ACCOUNT_IS_PRIVATE = By.xpath("//*[text()='This Account is Private']");

    public static final String RADU_VD_1_USERNAME = "cmFkdXZkMQ=="; //not ready yet for multiple users
    public static final String RADU_VD_USERNAME = "ci52YW5jZWFAeWFob28uY29t";

    public static String createPageAddress(String relativeUrl) {
        return PageAddress.INSTAGRAM_RAW.getLinkToPage() + relativeUrl;
    }
}
