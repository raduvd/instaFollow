package ro.personal.home.instafollow.enums;

public enum PageAddress {

    INSTAGRAM_RAW("https://www.instagram.com/"),
    INSTAGRAM_MY_ACCOUNT("https://www.instagram.com/raduvd"),
    PE_PLAIURI_ROMANESTI ("https://www.instagram.com/peplaiuri_romanesti/"),
    GENERAL(null);

    private PageAddress(String linkToPage) {
        this.linkToPage = linkToPage;
    }

    private String linkToPage;

    public String getLinkToPage() {
        return linkToPage;
    }
}
