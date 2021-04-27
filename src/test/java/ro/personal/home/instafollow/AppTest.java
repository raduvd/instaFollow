package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.Following;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;
import ro.personal.home.instafollow.persistance.repository.ProcessedPictureJpaRepository;
import ro.personal.home.instafollow.service.*;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class AppTest {

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
    public void testIsNumberOfRemovalsPerDayReached() {
        Assert.assertFalse(potentialFollowersService.isNumberOfRemovalsPerDayReached());
    }
}
