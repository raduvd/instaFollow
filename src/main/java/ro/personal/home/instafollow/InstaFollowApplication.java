package ro.personal.home.instafollow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
//TODO test the LISTS!!! (FOLLOWWER, FOLLOWING AND lIKES lists to see if they are processed corectly and saved in DB and all)
//TODO commit mandatory!!!
//TODO - after some time check what accounts did not followed back and adjust validation of follow request sending. THIS IS MANDATORY BECAUSE I HAVE some high values over there and they need to be tighten
*/
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ro.personal.home.instafollow.persistance.repository")
public class InstaFollowApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstaFollowApplication.class, args);
    }
//
}
