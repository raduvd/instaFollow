package ro.personal.home.instafollow.webDriver.model.elements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public abstract class PicturePage {

//    public static final By ELEMENT_PRICE_CURENCY = By.xpath(".//span[@itemprop= 'priceCurrency']");
//    public static final By ELEMENT_PRET = By.xpath(".//div[@class= 'pret']/span");
//    //Acest pret e calculat de Imobiliare, e ca si cum anuntul nu are pret, deci il voi considera invalid si
//    // elementul de mai jos nu il voi folosi nicaieri dar il las aici pentru reminder.
//    public static final By ELEMENT_PRET_WITH_EXTRA_DIV = By.xpath(".//div[@class= 'pret']/div/span");
//
//    protected WebElement elementulAnunt;
//    protected PageType pageType;
//    protected String priceCurrency;
//    protected BigDecimal pretPeMetruPatrat;
//    protected BigDecimal pret;
//    protected BigDecimal metriPatrati;
//    protected Result result;
//    protected String id;
//
//    public PicturePage(WebElement elementulAnunt, PageType pageType, Result result) {
//        this.result = result;
//        this.pageType = pageType;
//        this.elementulAnunt = elementulAnunt;
//        this.priceCurrency = getPriceCurrencyFromElement();
//        this.id = getIdFromElement();
//    }
//
//    /**
//     * Validarile le fac si pentru a ma asigura ca datele ce le am sunt valide.
//     * Spre exemplu un metruPatrat cu valoarea de 123000 nu e un metru patrat, nu e data valide.
//     * <p>
//     * Totodata fac aceste validari pentru a nu mi se strica calculele.
//     * Spre exemplu un penthouse cu pretul de 1milion de dolari imi strica calculele de medie, astfel mai bine il scot.
//     */
//    public abstract boolean validateAnunt();
//
//    public abstract BigDecimal getPretPeMetruPatratFromElement();
//
//    protected boolean validatePriceCurrency(PageType pageType) {
//        if (priceCurrency == null || !priceCurrency.equals("EUR")) {
//            result.add(ErrorType.INVALID_VALUE, priceCurrency, ElementValue.TEXT, "Currency non-valid", null, pageType);
//            return false;
//        }
//        return true;
//    }
//
//    protected boolean validatePretPeMetruPatrat(PageType pageType) {
//        if (pretPeMetruPatrat == null) {
//            result.add(ErrorType.INVALID_VALUE, pretPeMetruPatrat.toString(), null, "pretPeMetruPatrat null", null, pageType);
//            return false;
//        }
//        return true;
//    }
//
//    protected Object getValueFromElement(By locator, By fallBackLocator, ElementValue elementValue) {
//
//        WebElement element;
//
//        try {
//            try {
//                element = elementulAnunt.findElement(locator);
//            } catch (Exception e) {
//                element = elementulAnunt.findElement(fallBackLocator);
//            }
//        } catch (Exception e) {
//            result.add(ErrorType.ELEMENT_NOT_FOUND, locator.toString() + "SI FALBACK: " + fallBackLocator.toString(), elementValue, e.getMessage(), e, PageType.GENERAL);
//            return null;
//        }
//
//        String stringValue = element.getText();
//
//        if (stringValue == null || stringValue.isEmpty())
//            return null;
//
//        stringValue = stringValue.split(" ")[0];
//
//        switch (elementValue) {
//            case PRET_CU_PUNCT:
//                stringValue = stringValue.replace(".", "");
//                return getBigDecimalOutOfString(stringValue, elementValue);
//            case PRET_CU_VIRGULA:
//                stringValue = stringValue.replace(".", "");
//                stringValue = stringValue.replace(",", ".");
//                return getBigDecimalOutOfString(stringValue, elementValue);
//            case METRI_PATRATI:
//                return getBigDecimalOutOfString(stringValue, elementValue);
//            case TEXT:
//            default:
//                return stringValue;
//        }
//    }
//
//    private BigDecimal getBigDecimalOutOfString(String stringValue, ElementValue elementValue) {
//        BigDecimal bigDecimal;
//        try {
//            bigDecimal = new BigDecimal(stringValue);
//        } catch (Exception e) {
//            result.add(ErrorType.CASTING_EXCEPTION, stringValue, elementValue, e.getMessage(), e, PageType.GENERAL);
//            return null;
//        }
//        return bigDecimal;
//    }
//
//    public String getPriceCurrencyFromElement() {
//        return (String) getValueFromElement(ELEMENT_PRICE_CURENCY, ELEMENT_PRICE_CURENCY, ElementValue.TEXT);
//    }
//
//    public abstract BigDecimal getMetripatratiFromElement();
//
//    /**
//     * Unele anunturi au pretul fara TVA, dar TVA-ul la locuinte e complicat, e 5% pentru locuinte sub 120 mp si
//     * conditionat daca esti familie si multe alte chesti. Deci trebuie din pacate sa ignor acest aspect.
//     *
//     * @return
//     */
//    public BigDecimal getPretFromElement() {
//        return (BigDecimal) getValueFromElement(ELEMENT_PRET, ELEMENT_PRET, ElementValue.PRET_CU_PUNCT);
//    }
//
//    public Boolean validatePret(Double largerThen, Double smallerThen, PageType pageType) {
//
//        if (pret == null
//                || pret.doubleValue() < largerThen
//                || pret.doubleValue() > smallerThen) {
//
//            result.add(ErrorType.INVALID_VALUE, pret == null ? null : pret.toString(), null, "pret non-valid", null, pageType);
//            return false;
//        }
//        return true;
//    }
//
//    public Boolean validateMetriPatrati(Double largerThen, Double smallerThen, PageType pageType) {
//        if (metriPatrati == null
//                || metriPatrati.doubleValue() < largerThen
//                || metriPatrati.doubleValue() > smallerThen) {
//            result.add(ErrorType.INVALID_VALUE, metriPatrati == null ? null : metriPatrati.toString(), null, "metruPatrat non-valid", null, pageType);
//            return false;
//        }
//        return true;
//    }
//
//    public String getIdFromElement() {
//        String id = elementulAnunt.getAttribute("id");
//        if (id == null || id.isEmpty() || !id.contains("-"))
//            return null;
//        final String[] split = id.split("-");
//        id = split[1];
//        return id;
//    }
//
//    public boolean validateId() {
//        if (id == null || id.length() < 7 || id.length() > 11) {
//            result.add(ErrorType.INVALID_VALUE, id == null ? null : id.toString(), null, "id non-valid", null, pageType);
//            return false;
//        }
//        return true;
//    }
}
