package ro.personal.home.instafollow.webDriver.webDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WaitDriver {

    public static final Integer SECONDS_TO_WAIT_FOR_ELEMENT_TO_APPEAR = Integer.valueOf(5);

    public static WebDriverWait getWaitDriver(int seconds) {

        return new WebDriverWait(AppWebDriver.getWebDriver(), seconds);
    }

    public static WebElement waitAndGetElement(Boolean continueOnError, By... locator) {
        By locatorPresent = waitForElement(continueOnError, locator);

        if (locatorPresent == null) {
            if (continueOnError == false) {
                throw new RuntimeException("Continue on error is false and no element was found using locator: " + locator.toString());
            }
            return null;
        }
        return AppWebDriver.getWebDriver().findElement(locatorPresent);
    }

    public static List<WebElement> waitAndGetElements(Boolean continueOnError, By locator) {

        waitForElement(continueOnError, locator);
        return AppWebDriver.getWebDriver().findElements(locator);
    }

    public static By waitForElement(Boolean continueOnError, By... locators) {

        By locatorFound = null;
        int locatorsSize = locators.length;

        if (locatorsSize == 0)
            throw new RuntimeException("Method was not designed for this.");

        for (int i = 0; i < locatorsSize; i++) {
            try {
                WaitDriver.getWaitDriver(SECONDS_TO_WAIT_FOR_ELEMENT_TO_APPEAR).
                        until(ExpectedConditions.presenceOfElementLocated(locators[i]));
                return locators[i];
            } catch (Exception e) {
                if (i + 1 >= locatorsSize && !continueOnError)
                    throw new RuntimeException("The element was not found using the following locatos: " +
                            Arrays.stream(locators).map(by -> by.toString()).collect(Collectors.toList()));
            }
        }
        //If we continue on error this will be null
        return locatorFound;
    }

    public static void sleepForMiliseconds(Integer miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
