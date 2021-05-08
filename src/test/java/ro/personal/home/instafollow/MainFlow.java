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
    public void wholeFlow() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);

        //Process followers list before adding new accounts, because it is more error free
        processListService.refreshFollowers();

        //MANDATORY RUN REFRESH FOLLOWERS BEFORE REMOVING
        processListService.processFollowingListAndRemoveAccountsThatDoNotFollowBack();

        //Best to be rolled after refreshingFollowers and removing followers!!!
        potentialFollowersService.analiseFollowRequestResults();

        //processListService.savePotentialFollowersFrom(PageAddress.PE_PLAIURI_ROMANESTI, 3);

        //potentialFollowersService.followPotentialFollowers(Integer.MAX_VALUE);
    }

    @Test
    public void step1RefreshFollowers() {

        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        processListService.refreshFollowers();
    }

    @Test
    public void step2RemoveAccountsThatDoNotFollowBack() {

        //Process followers list before adding new accounts, because it is more error free
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        //MANDATORY RUN REFRESH FOLLOWERS BEFORE REMOVING
        processListService.processFollowingListAndRemoveAccountsThatDoNotFollowBack();
    }

    @Test
    public void step3ConfirmRemovals() {

        //TODO run this only if there are inconsistencies (if not all were removed)
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        potentialFollowersService.confirmRemovedUsers(2);
    }

    @Test
    public void step4SavePotentialFollowers() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        processListService.savePotentialFollowersFrom(PageAddress.PE_PLAIURI_ROMANESTI, 0, 1);
    }

    @Test
    public void step5FollowPotentialFollowers() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        potentialFollowersService.followPotentialFollowers(Integer.MAX_VALUE);
    }

    @Test
    public void step6AnaliseFollowRequestResults() {
        //Best to be rolled after refreshingFollowers and removing followers!!!
        potentialFollowersService.analiseFollowRequestResults();
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }

    @Test
    public void processPotentialFollowers() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, RADU_VD_USERNAME, true);
        potentialFollowersService.step3RemoveOrFollow();
    }
}
