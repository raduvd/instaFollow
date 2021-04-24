package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;
import ro.personal.home.instafollow.persistance.repository.FollowingJpaRepository;
import ro.personal.home.instafollow.persistance.repository.ProcessedPictureJpaRepository;
import ro.personal.home.instafollow.service.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class AppTest {

    @Autowired
    private WebDriverService webDriverService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private FollowingService followingService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    @Autowired
    private ProcessedPictureJpaRepository processedPictureJpaRepository;

    @Test
    public void testGetFollowersFromDB() {
        List<PotentialFollower> potentialFollowerForFollowing = potentialFollowersService.getPotentialFollowerForFollowing();
        Assert.assertNotNull(potentialFollowerForFollowing);
        Assert.assertFalse(potentialFollowerForFollowing.isEmpty());
        Assert.assertTrue(
                potentialFollowerForFollowing.stream().
                        allMatch(potentialFollower ->
                                potentialFollower.getIsFollowRequested() == false
                                        && potentialFollower.getIsRejectedDueToValidation() == false));
    }

    @Test
    public void testIsNoMoreSave() {
        followingService.setIsNoMore();
        followerService.setIsNoMore();
    }
}
