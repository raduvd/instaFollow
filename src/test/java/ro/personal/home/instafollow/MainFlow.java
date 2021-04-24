package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.service.AccountService;
import ro.personal.home.instafollow.service.WebDriverService;
import ro.personal.home.instafollow.webDriver.model.Page;

import java.util.Collections;

@SpringBootTest
public class MainFlow {

    @Autowired
    private WebDriverService webDriverService;

    @Autowired
    private AccountService accountService;

    private static final String RADU_VD_USERNAME = "ci52YW5jZWFAeWFob28uY29t";
    private static final String RADU_VD_1_USERNAME = "cmFkdXZkMQ==";

    @Test
    public void saveFollowersAndFollowing() {
        Page page = new Page(PageAddress.INSTAGRAM_RAW, accountService.getAccount(RADU_VD_USERNAME));
        webDriverService.saveFollowersAndFollowing(page);
    }

    @Test
    @Ignore
    public void savePotentialFollowers() {
        Page page = new Page(PageAddress.INSTAGRAM_RAW, accountService.getAccount(RADU_VD_USERNAME));
        webDriverService.savePotentialFollowersFrom(Collections.singletonList(PageAddress.PE_PLAIURI_ROMANESTI), 1, page);
    }

    @Test
    @Ignore
    public void followPotentialFollowers() {
        Page page = new Page(PageAddress.INSTAGRAM_RAW, accountService.getAccount(RADU_VD_USERNAME));
        webDriverService.followPotentialFollowers(page, Integer.MAX_VALUE);
    }

    @Test
    @Ignore
    public void removeFollowersThatDoNotFollowBack() {
        Page page = new Page(PageAddress.INSTAGRAM_RAW, accountService.getAccount(RADU_VD_USERNAME));
        webDriverService.removeFollowersThatDoNotFollowBack();
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }

    @Test
    public void loginWithCookies() {

        Page page = new Page(PageAddress.PE_PLAIURI_ROMANESTI, accountService.getAccount(RADU_VD_1_USERNAME));
    }
}
