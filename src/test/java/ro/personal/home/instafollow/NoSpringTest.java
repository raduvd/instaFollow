package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;
import ro.personal.home.instafollow.service.FollowerService;

import java.util.Optional;

public class NoSpringTest {

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
