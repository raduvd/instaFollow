package ro.personal.home.instafollow;

import lombok.Data;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.service.*;

@Data
@SpringBootTest
public class MainFlow {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PageService pageService;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private ProcessListService processListService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    @Test
    public void wholeFlow() {
        pageService.initializePage(PageAddress.INSTAGRAM_RAW, WebDriverUtil.RADU_VD_USERNAME, true);
        processListService.refreshFollowers(true);
        potentialFollowersService.step3RemoveOrFollow();
        potentialFollowersService.step4Follow();
        processListService.removeNonFollowers(1);
        potentialFollowersService.analiseFollowRequestResults();
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }
}
