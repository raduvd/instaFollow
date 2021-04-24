package ro.personal.home.instafollow.webDriver.model;

import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.ErrorType;
import ro.personal.home.instafollow.enums.PageAddress;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@Builder
@ToString
public class Errors implements Serializable {

    ErrorType errorType;
    String value;
    ElementValue elementValue;
    String errorMessage;
    Exception exception;
    PageAddress pageAddress;
}
