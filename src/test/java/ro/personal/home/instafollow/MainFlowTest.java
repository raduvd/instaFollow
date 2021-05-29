package ro.personal.home.instafollow;

import lombok.Data;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.service.*;

@Data
@SpringBootTest
public class MainFlowTest {

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

    @Autowired
    private MailService mailService;

    @Test
    public void wholeFlow() {
        Exception failure = null;
        try {
            pageService.initializePage(PageAddress.INSTAGRAM_RAW, WebDriverUtil.RADU_VD_USERNAME, true);
            processListService.refreshFollowers(true);
            potentialFollowersService.step3RemoveOrFollow();
            potentialFollowersService.step4Follow();
            processListService.removeNonFollowers(1);
        } catch (Exception e) {
            failure = e;
        } finally {
            if (failure == null) {
                mailService.sendSimpleMessage("SUCCESS",
                        potentialFollowersService.analiseFollowRequestResults());
            } else {
                mailService.sendSimpleMessage("FAILURE", failure.getMessage());
            }
        }
    }

    @Test
    @Ignore
    public void createNewAccountDBEntry() {
        accountService.saveAccount("", "");
    }
}
