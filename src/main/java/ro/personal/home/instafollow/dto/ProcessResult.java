package ro.personal.home.instafollow.dto;

import lombok.Data;
import ro.personal.home.instafollow.enums.Process;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ProcessResult {

    public ProcessResult(AtomicInteger leftToIterate) {
        this.leftToIterate = leftToIterate;
    }

    private static AtomicInteger ZERO = new AtomicInteger(0);
    private AtomicInteger followed = ZERO;
    private AtomicInteger removed = ZERO;
    private AtomicInteger confirmedRemoved = ZERO;
    private AtomicInteger confirmedFollowing = ZERO;
    private AtomicInteger leftToIterate;

    public void printResultAndConfirmProcessType(Process process) {

        switch (process) {
            case CONFIRM_REMOVING:
                System.out.println("WE JUST RE-REMOVED AGAIN: " + removed);
                System.out.println("We confirmed removed: " + confirmedRemoved);
                if (followed.get() != 0 || confirmedFollowing.get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just confirm removal or remove again");
                }
                break;
            case CONFIRM_FOLLOWING:
                System.out.println("WE JUST RE-FOLLOWED AGAIN: " + followed);
                System.out.println("We confirmed followings: " + confirmedFollowing);
                if (removed.get() != 0 || confirmedRemoved.get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just confirm follow or follow again");
                }
                break;
            case REMOVING_OR_FOLLOWING:
                System.out.println("WE JUST RE-FOLLOWED AGAIN: " + followed);
                System.out.println("WE JUST REMOVED FOR THE FIRST TIME: " + removed);
                if (confirmedRemoved.get() != 0 || confirmedFollowing.get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow or remove, not confirmed");
                }
                break;
            case FOLLOWING:
                System.out.println("WE JUST FOLLOWED FOR THE FIRST TIME: " + followed);
                if (removed.get() != 0 || confirmedRemoved.get() != 0 || confirmedFollowing.get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow and anything else.");
                }
                break;
            default:
                throw new RuntimeException("Not existing process. We are not supposed to reach here.");
        }
    }
}