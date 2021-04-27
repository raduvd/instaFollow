package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;
import ro.personal.home.instafollow.service.FollowerService;
import ro.personal.home.instafollow.service.PotentialFollowersService;
import ro.personal.home.instafollow.service.ProcessListService;

import java.time.LocalDate;
import java.util.Optional;

public class NoSpringTest {

    @Test
    public void testIsUserValidForRemovalWithEmpthyOptional() {
        Assert.assertFalse(testIsUserValidForRemoval(Optional.empty(), false, false));
    }

    @Test
    public void testIsUserValidForRemovalWithWrongPotentialFollower() {
        Assert.assertFalse(testIsUserValidForRemoval(Optional.of(new PotentialFollower()), false, false));
    }

    @Test
    public void testIsUserValidForRemovalWithWrongPotentialFollower2() {
        PotentialFollower potentialFollower = new PotentialFollower();
        potentialFollower.setFollowRequestSentAtDate(LocalDate.now().minusDays(1));
        Assert.assertFalse(testIsUserValidForRemoval(Optional.of(potentialFollower), false, false));
    }

    @Test
    public void testIsUserValidForRemovalWithGoodPotentialFollower2() {
        PotentialFollower potentialFollower = new PotentialFollower();
        potentialFollower.setFollowRequestSentAtDate(LocalDate.now().minusDays(3));
        Assert.assertTrue(testIsUserValidForRemoval(Optional.of(potentialFollower), false, false));
    }

    @Test
    public void testIsUserValidForRemovalWithGoodPotentialAndNumberReached() {
        PotentialFollower potentialFollower = new PotentialFollower();
        potentialFollower.setFollowRequestSentAtDate(LocalDate.now().minusDays(3));
        Assert.assertFalse(testIsUserValidForRemoval(Optional.of(potentialFollower), true, false));
    }

    @Test
    public void testIsUserValidForRemovalWithGoodPotentialAndUserNotAFollower() {
        PotentialFollower potentialFollower = new PotentialFollower();
        potentialFollower.setFollowRequestSentAtDate(LocalDate.now().minusDays(3));
        Assert.assertFalse(testIsUserValidForRemoval(Optional.of(potentialFollower), false, true));
    }
    private boolean testIsUserValidForRemoval(Optional<PotentialFollower> potentialFollowerOptional, boolean isNumberOfRemovalsPerDayReached, boolean isUserInMyFollowerList) {

        PotentialFollowersService potentialFollowersServiceMock = Mockito.mock(PotentialFollowersService.class);
        FollowerService followerServiceMock = Mockito.mock(FollowerService.class);

        Mockito.when(potentialFollowersServiceMock.getOptionalById("test")).thenReturn(potentialFollowerOptional);
        Mockito.when(potentialFollowersServiceMock.isNumberOfRemovalsPerDayReached()).thenReturn(isNumberOfRemovalsPerDayReached);
        Mockito.when(followerServiceMock.isUserInMyFollowerList("test")).thenReturn(isUserInMyFollowerList);

        return new ProcessListService(null, potentialFollowersServiceMock, null, followerServiceMock, null).
                removeUserQuestionMark("test");
    }

    @Test
    public void testIsUserInMyFollowerList() {

        FollowersJpaRepository followersJpaRepositoryMock = Mockito.mock(FollowersJpaRepository.class);
        Followers followers = new Followers();
        followers.setIsNoMore(false);
        Optional<Followers> optionalFollowers = Optional.of(followers);

        Mockito.when(followersJpaRepositoryMock.findById("test")).thenReturn(optionalFollowers);
        boolean followerValid = new FollowerService(followersJpaRepositoryMock).isUserInMyFollowerList("test");

        Assert.assertTrue(followerValid);
    }
}
