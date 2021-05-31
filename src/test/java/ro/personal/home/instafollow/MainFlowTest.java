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
public class MainFlowTest {

    Logger logger = LoggerFactory.getLogger(MainFlowTest.class);

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
            //I comented this out because the I was blocked by instagram because to many requests, this is something that can be removed
            //potentialFollowersService.step3RemoveOrFollow();
            potentialFollowersService.step4Follow();
            processListService.removeNonFollowers(0);
        } catch (Exception e) {
            failure = e;
            logger.error(e.getMessage(), e);
        } finally {
            if (failure == null) {
                mailService.sendSimpleMessage("SUCCESS",
                        potentialFollowersService.analiseFollowRequestResults(
                                PotentialFollowersService.ANALYSE_FROM_DATE));
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
