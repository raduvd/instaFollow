package ro.personal.home.instafollow.webDriver.model;


import lombok.Data;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.ErrorType;
import ro.personal.home.instafollow.enums.PageAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Result implements Serializable {

    //    private Integer numarAnunturiProcesate = 0;
//    private Integer numarulDePaginiProcesate = 0;
    private List<Errors> allErrorsList = new ArrayList<>();
    private List<Errors> invalidValueList = new ArrayList<>();
    private List<Errors> castingExceptionList = new ArrayList<>();
    private List<Errors> elementNotFoundList = new ArrayList<>();
    private List<Errors> waitingTimeoutList = new ArrayList<>();
    private List<Errors> errorInJavaLogicList = new ArrayList<>();
    private List<String> messages= new ArrayList<>();

    public void add(ErrorType errorType, String value, ElementValue elementValue, String errorMessage, Exception exception, PageAddress impobilType) {
        if (value == null)
            value = "null";

        if (errorMessage != null && errorMessage.length() > 130) {
            errorMessage = errorMessage.substring(0, 129);
        }
        final Errors error = Errors.builder().
                errorType(errorType).
                value(value).
                elementValue(elementValue).
                errorMessage(errorMessage).
                exception(exception).
                pageAddress(impobilType).
                build();

        allErrorsList.add(error);

        switch (errorType) {
            case INVALID_VALUE:
                invalidValueList.add(error);
                break;
            case WAITING_TIMEOUT:
                waitingTimeoutList.add(error);
                break;
            case CASTING_EXCEPTION:
                castingExceptionList.add(error);
                break;
            case ELEMENT_NOT_FOUND:
                elementNotFoundList.add(error);
                break;
            case ERROR_IN_JAVA_LOGIC:
                errorInJavaLogicList.add(error);
        }
    }
//
//    public Integer addToNumarAnunturiProcesate(Integer numberToAdd) {
//
//        numarAnunturiProcesate = numarAnunturiProcesate + numberToAdd;
//        return numarAnunturiProcesate;
//    }
//
//    public void incrementnumarulDePaginiProcesate() {
//        numarulDePaginiProcesate++;
//    }
}
