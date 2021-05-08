package ro.personal.home.instafollow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
THE APP SERVICES ARE WORKING JUST SOME DETAILS / tasks / improvements:
//TODO - the potentialFOllower table is not consistent because isFollowRequested and
   followRequestSentAtDate are filled but the request is not sent because some instagram restriction. I use the table to devise the report and other places
   All should be good but the report where I should not use these fields, but the confirmed removed accounts and the followers that I have.
//TODO - after some time check what accounts did not followed back and adjust validation of follow request sending. THIS IS MANDATORY BECAUSE I HAVE some high values over there and they need to be tighte
//TODO go trough all files and:
        - check for all todos
        - check logic, how can it be imrpoved:
            - make a final review and add todos
            - the errors that you throw are not nice, make it another way
        - check warnings, and remove
        - remove all unused methods, and fields
        - breakdonww classes and methods into smaller - each method should have a single logic and to be clear
        - commit
//TODO replace all prints with logs:
    - use spring logging https://www.baeldung.com/spring-boot-logging
    - Make 2 or 3 kinds of logs. only the most important should be put in console, the rest in a log file
    - FOR THIS REMOVE THE rESULT ENTITY? AND THE PRINT LOGIC? SEE WHERE IT IS USED
//TODO - The App is build for raduvd: there are By locators that specifically search this username + the DB tables handle one account
//TODO - when processing list I must not move the mouse around, because there will be errors.
//TODO ◦ When the app is really stable, Automate to maximum by following the steps:
            - Run the test with just one single tiny command
            - Use the app for like a week and debug When it fails
            - Make bash script with this single tiny mvn command (https://gist.github.com/shsteimer/a4a4b1c865830fdd5e41872cd2767106) )
            or just set it to run if hour is at night https://www.ionos.com/digitalguide/server/know-how/how-to-write-a-basic-shell-script/ )
            or just set it to run if hour is at night https://stackoverflow.com/questions/1885525/how-do-i-prompt-a-user-for-confirmation-in-bash-script )
            or just set it to run if hour is at night https://superuser.com/questions/954950/run-a-script-on-start-up-on-windows-10https://superuser.com/questions/954950/run-a-script-on-start-up-on-windows-10
            - Make windows boot for an hour or two in the night and boot off https://lifehacker.com/how-can-i-start-and-shut-down-my-computer-automatically-5831504
*/
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ro.personal.home.instafollow.persistance.repository")
public class InstaFollowApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstaFollowApplication.class, args);
    }
//
}
