package ro.personal.home.instafollow.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.ProcessResult;
import ro.personal.home.instafollow.persistance.repository.ProcessResultJpaRepository;
import ro.personal.home.instafollow.persistance.specification.SpecificationUtil;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Service
public class ProcessResultService {

    Logger logger = LoggerFactory.getLogger(ProcessResultService.class);

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    @Autowired
    private ProcessResultJpaRepository processResultJpaRepository;

    public void printResultAndValidate(Process process, ProcessResult processResult) {

        switch (process) {
            case CONFIRM_REMOVING:
                logger.info("WE JUST RE-REMOVED AGAIN: " + processResult.getRemoved());
                logger.info("We confirmed removed: " + processResult.getConfirmedRemoved());
                if (processResult.getFollowed() != 0 || processResult.getConfirmedFollowing() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just confirm removal or remove again");
                }
                break;
            case REMOVING_OR_FOLLOWING:
                logger.info("WE JUST RE-FOLLOWED AGAIN: " + processResult.getFollowed());
                logger.info("WE JUST REMOVED FOR THE FIRST TIME: " + processResult.getRemoved());
                if (processResult.getConfirmedRemoved() != 0 || processResult.getConfirmedFollowing() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow or remove, not confirmed");
                }
                break;
            case FOLLOWING:
                logger.info("WE JUST FOLLOWED FOR THE FIRST TIME: " + processResult.getFollowed());
                if (processResult.getRemoved() != 0 || processResult.getConfirmedRemoved() != 0
                        || processResult.getConfirmedFollowing() != 0) {
                    throw new RuntimeException("Something wrong with the logic, here we supposed to just follow and anything else.");
                }
                break;
            case REMOVE_NON_FOLLOWERS:
                logger.info("The 'Followings' list size, before processing was: " + processResult.getConfirmedFollowing());
                logger.info("I went trough it and processed: " + processResult.getTotalProcessedUsers());
                logger.info("From these users I removed: " + processResult.getFromTotalWeAppliedLogicOn());
                logger.info("I should now have followers: " +
                        (processResult.getConfirmedFollowing() - processResult.getFromTotalWeAppliedLogicOn()));

                if (processResult.getTotalProcessedUsers() < processResult.getConfirmedFollowing() - 5
                        && !potentialFollowersService.isNumberOfRemovalsPerDayReached()) {
                    throw new RuntimeException("The remove non follower process has failed!" +
                            " The list was not entirely processed.");
                } else {
                    logger.info("The process is complete and successfully... saving in DB.");
                }
                break;
            default:
                throw new RuntimeException("Not supported process. We are not supposed to reach here.");
        }
        processResult.setAddedAt(LocalDateTime.now());
        processResult.setProcessType(process.toString());
        if (!Integer.valueOf(0).equals(processResult.getConfirmedFollowing()) ||
                !Integer.valueOf(0).equals(processResult.getRemoved()) ||
                !Integer.valueOf(0).equals(processResult.getFollowed()) ||
                !Integer.valueOf(0).equals(processResult.getConfirmedRemoved()))
            processResultJpaRepository.saveAndFlush(processResult);
    }

    public List<ProcessResult> getAllInIntervalOfDays(Integer numberOfDaysInterval, Process process) {
        return processResultJpaRepository.
                findAll(SpecificationUtil.getProcessResultsInNumberOfDays(numberOfDaysInterval).and(
                        SpecificationUtil.getProcessResultsByProcessType(process)));
    }

    public List<ProcessResult> getAllProcessesFromToday() {
        return processResultJpaRepository.
                findAll(SpecificationUtil.getProcessResultsInNumberOfDays(0));
    }
}