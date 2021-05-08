package ro.personal.home.instafollow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ro.personal.home.instafollow.persistance.repository.ProcessedPictureJpaRepository;
import ro.personal.home.instafollow.service.AccountService;
import ro.personal.home.instafollow.service.FollowerService;
import ro.personal.home.instafollow.service.FollowingService;
import ro.personal.home.instafollow.service.PotentialFollowersService;

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
    public void test() {
    }
}
