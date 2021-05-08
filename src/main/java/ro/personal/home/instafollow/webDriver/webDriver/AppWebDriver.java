package ro.personal.home.instafollow.webDriver.webDriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

@Component
public class AppWebDriver {
    /**
     * Aici am pathul local (de pe PC) catre chromedriver.exe
     */
    private static final String CHROMEDRIVER_EXECUTABLE = "C:\\Users\\rvanc\\Desktop\\Libraryes & Sources\\chromedriver_win32\\chromedriver.exe";
    private static org.openqa.selenium.WebDriver webDriver;

    private AppWebDriver() {
    }

    public static org.openqa.selenium.WebDriver getWebDriver() {
        if (webDriver == null) {
            System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_EXECUTABLE);
            webDriver = new ChromeDriver();
            //webDriver.manage().window().setPosition(new Point(-2000, 0));
        }
        return webDriver;
    }

    public static void closeWebDriver() {
        if(webDriver == null)
            return;
        webDriver.close();
        webDriver = null;
    }

    public static void refreshPage() {
        webDriver.navigate().refresh();
    }
}



