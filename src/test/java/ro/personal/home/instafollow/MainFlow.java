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
        processListService.refreshFollowers();
        potentialFollowersService.step2ConfirmRemovals();
        potentialFollowersService.step3RemoveOrFollow();
        potentialFollowersService.step4Follow();
        potentialFollowersService.analiseFollowRequestResults();
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }
}
