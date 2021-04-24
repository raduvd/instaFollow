package ro.personal.home.instafollow;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.service.WebDriverService;
import ro.personal.home.instafollow.webDriver.model.Page;

public class NoSpringTest {

    @Test
    public void decodeTest() {
        Page pageMock = Mockito.mock(Page.class);
        //following
        Mockito.when(pageMock.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, WebDriverService.FOLLOWERS_NUMBER, WebDriverService.FOLLOWER_NUMBER)).
                thenReturn(Integer.valueOf(1500));
        //followers
        Mockito.when(pageMock.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, WebDriverService.FOLLOWING_NUMBER)).
                thenReturn(Integer.valueOf(1300));
        //posts
        Mockito.when(pageMock.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, WebDriverService.POSTS_NUMBER, WebDriverService.POST_NUMBER)).
                thenReturn(Integer.valueOf(501));

        boolean followerValid = new WebDriverService().isFollowerValid(pageMock, new PotentialFollower());

        Assert.assertTrue(followerValid);
    }
}
