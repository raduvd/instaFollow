package ro.personal.home.instafollow.service;

import org.openqa.selenium.By;
import ro.personal.home.instafollow.enums.ErrorType;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.webDriver.model.Errors;
import ro.personal.home.instafollow.webDriver.model.Result;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WebDriverUtil {

    public static final By PAGE_PICTURES = By.xpath("//*[@alt and @srcset]/../../..");
    public static final By CLOSE = By.xpath("//*[@aria-label='Close']");

    public static String createPageAddress(String relativeUrl) {
        return PageAddress.INSTAGRAM_RAW.getLinkToPage() + relativeUrl;
    }

    public static void printResultInConsoleLog(Result result) {
        System.out.println("------------------------------------ ..\nSUM-UP\n..\n");
        result.getMessages().forEach(System.out::println);
        System.out.println("------------------------------------TOTAL number of errors: " + result.getAllErrorsList().size());
        System.out.println("------------------------------------" + ErrorType.INVALID_VALUE.name() + " - number of errors: " + result.getInvalidValueList().size() + " ---- description: " + ErrorType.INVALID_VALUE.description);
        System.out.println("------------------------------------" + ErrorType.WAITING_TIMEOUT.name() + " - number of errors: " + result.getWaitingTimeoutList().size() + " ---- description: " + ErrorType.WAITING_TIMEOUT.description);
        System.out.println("------------------------------------" + ErrorType.CASTING_EXCEPTION.name() + " - number of errors: " + result.getCastingExceptionList().size() + " ---- description: " + ErrorType.CASTING_EXCEPTION.description);
        System.out.println("------------------------------------" + ErrorType.ELEMENT_NOT_FOUND.name() + " - number of errors: " + result.getElementNotFoundList().size() + " ---- description: " + ErrorType.ELEMENT_NOT_FOUND.description);
        System.out.println("------------------------------------" + ErrorType.ERROR_IN_JAVA_LOGIC.name() + " - number of errors: " + result.getErrorInJavaLogicList().size() + " ---- description: " + ErrorType.ERROR_IN_JAVA_LOGIC.description);

        final List<Errors> sortedAllErrorList = result.getAllErrorsList().stream().
                sorted(Comparator.comparing(Errors::getValue)).collect(Collectors.toList());

        System.out.println("..\nSHORT ERRORS\n..\n");
        sortedAllErrorList.forEach(e -> System.out.println(e.getValue() + " - " + e.getErrorType() + " - " + e.getErrorMessage() + " - " + e.getPageAddress().name()));

        Set<Errors> uniqueValueErrors = new TreeSet<>(Comparator.comparing(Errors::getValue));
        uniqueValueErrors.addAll(sortedAllErrorList);

        System.out.println("..\nSTACK TRACES\n..\n");
        uniqueValueErrors.forEach(e -> {
            if (e.getException() != null) {
                System.out.println(e.getValue() + " - " + e.getErrorType() + " - " + e.getErrorMessage() + " - " + e.getPageAddress().name());
                e.getException().printStackTrace();
            }
        });
    }
}
