package ro.personal.home.instafollow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
THE APP SERVICES ARE WORKING JUST SOME DETAILS / tasks / improvements:
//TODO the process list process is failing something has changed maybe
//TODO I get banned by instagram more offten as a solution:
    //eliminate the follow process and try to follow when you process the like list
    //confirm the followings when you go over the followers list
    //if some of the followings sending sent when processing the like list weere not registered it means that the user is not in the following list, so get all and this time only go manually on the page with the FOLLOW process
    //do not follow private if you do this because it is not consistent
    //make a logic to also like the first picture of the user that you will follow
//TODO DO NOT WORK ON THIS ANY MORE, I HAVE SPENT A LOT!! MAXIMUM AN HOUR ONCE A WEEK + and small debug!!!
//TODO ANALIZE PERIODICALLY
   - check in DB how many removals are made in the REMOVE_NON_FOLLOWER process. If is zero or low, make the interval bigger, run more rarely
   - check how many of the request (follow or/and remove) are not being processed, this is shown in the logs and
        db and maybe wait more and see if it improoves, or combine them, a follow then a removal and so on.
        Maybe at some point i can remove the confirmation
    - after a month or a week check in the DB- all should have confirmedRemoval = true and confirmedFoloowing = true,
        if not the logic is not good
    - after some time check what accounts did not followed back and adjust validation of follow request sending.
        THIS IS MANDATORY BECAUSE I HAVE some high values over there and they need to be tighte
        there is a problem here because almost all users are invalid so I must add 100 each day,
        I do not have enough valid users so maybe loosen up a
        little bit and recheck the invalid users from db and additionally change the validatiopn logic
//TODO the like list logic will run and run and run.
   i really process hundreds of users per day, today i processed 600(hovered over 350) and got like 40 valid users
   so rally need a limit of hovers per day in order not to be blocked0
   i need to throw an error if it runs withouth iterating trough all the list simillary with the
   also put a counter in the like list logic, like send how big is the list as a param in the likelistlogic and decrement or something
//TODO am impresia ca daca dau follow la private account multe nu sunt procesate de instagram. dar cele non private requesturuile sunt trimise, check this
//  aici ar trebui sa adug niste logica ca sa o salvez in db in processResult si apoi sa o verific periodic
//TODO go trough all files and:
        - check for all todos
        - check logic, how can it be imrpoved:
            - like you do at work before comiting to a reviewer
            - make a final review and add todos
            - the errors that you throw are not nice, make it another way
        - check warnings, and remove
        - remove all unused methods, and fields
        - breakdonww classes and methods into smaller - each method should have a single logic and to be clear
        - commit
//TODO - The App is build for raduvd: there are By locators that specifically search this username + the DB tables handle one account
*/
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ro.personal.home.instafollow.persistance.repository")
public class InstaFollowApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstaFollowApplication.class, args);
    }
//
}
