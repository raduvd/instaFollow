package ro.personal.home.instafollow.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.Result;
import ro.personal.home.instafollow.persistance.repository.ResultJpaRepository;

import java.time.LocalDateTime;

@Data
@Service
public class ResultService {

    @Autowired
    private ResultJpaRepository resultJpaRepository;

    public void printResultAndConfirmProcessType(Process process, Result result) {

        switch (process) {
            case CONFIRM_REMOVING:
                System.out.println("WE JUST RE-REMOVED AGAIN: " + result.getRemoved());
                System.out.println("We confirmed removed: " + result.getConfirmedRemoved());
                if (result.getFollowed().get() != 0 || result.getConfirmedRemoved().get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just confirm removal or remove again");
                }
                break;
            case REMOVING_OR_FOLLOWING:
                System.out.println("WE JUST RE-FOLLOWED AGAIN: " + result.getFollowed());
                System.out.println("WE JUST REMOVED FOR THE FIRST TIME: " + result.getRemoved());
                if (result.getConfirmedRemoved().get() != 0 || result.getConfirmedFollowing().get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow or remove, not confirmed");
                }
                break;
            case FOLLOWING:
                System.out.println("WE JUST FOLLOWED FOR THE FIRST TIME: " + result.getFollowed());
                if (result.getRemoved().get() != 0 || result.getConfirmedRemoved().get() != 0
                        || result.getConfirmedFollowing().get() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow and anything else.");
                }
                break;
            default:
                throw new RuntimeException("Not existing process. We are not supposed to reach here.");
        }
        result.setAddedAt(LocalDateTime.now());
        //TODO AtomicInteger does not work
        //resultJpaRepository.saveAndFlush(result);
    }
}