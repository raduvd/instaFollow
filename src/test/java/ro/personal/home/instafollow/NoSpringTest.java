package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;
import ro.personal.home.instafollow.service.FollowerService;
import ro.personal.home.instafollow.service.ProcessListService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class NoSpringTest {

    ProcessListService processListService = new ProcessListService();

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

    @Test
    public void testIsUserValid() {
        Assert.assertTrue(processListService.validateUserInfo(368, 400, 2));
        Assert.assertFalse(processListService.validateUserInfo(368, 400, 1));
        Assert.assertFalse(processListService.validateUserInfo(499, 488, 2));
        Assert.assertTrue(processListService.validateUserInfo(501, 488, 2));
        Assert.assertTrue(processListService.validateUserInfo(501, 488, 2));
        Assert.assertFalse(processListService.validateUserInfo(501, 435, 2));
        Assert.assertFalse(processListService.validateUserInfo(999, 933, 2));
        Assert.assertTrue(processListService.validateUserInfo(1499, 1500, 2));
        Assert.assertFalse(processListService.validateUserInfo(1499, 1368, 2));
        Assert.assertTrue(processListService.validateUserInfo(1499, 1370, 2));
        Assert.assertTrue(processListService.validateUserInfo(1501, 1368, 2));
        Assert.assertTrue(processListService.validateUserInfo(1501, 1302, 2));
    }

    @Test
    public void test() {
        LocalTime midnight = LocalTime.MIDNIGHT.plusHours(1);
        LocalDate today = LocalDate.now();
        LocalDateTime intervalOfDaysMidnight = LocalDateTime.of(today.minusDays(0), midnight);
        System.out.println(intervalOfDaysMidnight);
    }

}
