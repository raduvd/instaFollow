package ro.personal.home.instafollow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
THE APP IS FINISHED JUSST SOME DETAILS
//TODO - after some time check what accounts did not followed back and adjust validation of follow request sending. THIS IS MANDATORY BECAUSE I HAVE some high values over there and they need to be tighte
//TODO - when following add a 1 minute break because it is really suspicious
//TODO the prints are confusig, print just what you need to know and check the logs for the rest!!
//TODO check for all todos
//TODO make code nicer:
            make a final review and add todos
            the errors that you throw are not nice, make it another way
            do not use prints, make logs or use RESULT entity that you already have
//TODO as a last step after follow, remove is working make a cleanup: remove all unused methods, breakdonww maybe this class and do a final review and commit
//TODO - The App is build for raduvd: there are By locators that specifically search this username + the DB tables handle one account
//TODO commit mandatory!!!
//TODO - instagram will not remove all the users that I press remove on. But I will modify in the DB that I have removed them. Think if this will be a problem? and think of a way to fix this.
//TODO - when processing list I must not move the mouse around, because there will be errors.
*/
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ro.personal.home.instafollow.persistance.repository")
public class InstaFollowApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstaFollowApplication.class, args);
    }
//
}
