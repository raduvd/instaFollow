package ro.personal.home.instafollow.enums;

import java.math.BigDecimal;

public enum ElementValue {

    TEXT(String.class, null, null),
    NUMBER_WITH_K_COMA_OR_POINT(Integer.class, null, null);


    private ElementValue(Class<?> tipDeData, Boolean eliminateDot, Boolean replaceComaWithDot) {
        this.eliminateDot = eliminateDot;
        this.tipDeData = tipDeData;
        this.replaceComaWithDot = replaceComaWithDot;
    }

    private Class<?> tipDeData;
    private Boolean eliminateDot;
    Boolean replaceComaWithDot;
}
