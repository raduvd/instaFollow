package ro.personal.home.instafollow;

import lombok.Data;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.service.AccountService;
import ro.personal.home.instafollow.service.PageService;
import ro.personal.home.instafollow.service.PotentialFollowersService;
import ro.personal.home.instafollow.service.ProcessListService;

import java.util.Collections;

@Data
@SpringBootTest
public class MainFlow {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PageService pageService;

    @Autowired
    private ProcessListService processListService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    private static final String RADU_VD_USERNAME = "ci52YW5jZWFAeWFob28uY29t";
    //private static final String RADU_VD_1_USERNAME = "cmFkdXZkMQ=="; //not ready yet for multiple users

    @Test
    public void saveFollowersAndRemoveAccountsThatDoNotFollowBack() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        processListService.refreshFollowers();
        //MANDATORY RUN REFRESH FOLLOWERS BEFORE REMOVING
        processListService.processFollowingListAndRemoveAccountsThatDoNotFollowBack();
    }

    @Test
    public void savePotentialFollowers() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        processListService.savePotentialFollowersFrom(Collections.singletonList(PageAddress.INSTAGRAM_RAW), 0);
    }

    @Test
    public void followPotentialFollowers() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        potentialFollowersService.followPotentialFollowers(Integer.MAX_VALUE);
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }

    @Test
    public void analiseFollowRequestResults() {
        //Best to be rolled after refreshingFollowers and removing followers!!!
        potentialFollowersService.analiseFollowRequestResults();
    }

}
