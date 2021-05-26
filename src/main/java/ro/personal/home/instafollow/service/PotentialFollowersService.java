package ro.personal.home.instafollow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.Button;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.model.ProcessResult;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
public class PotentialFollowersService {

    private static StringBuilder RESULT;

    Logger logger = LoggerFactory.getLogger(PotentialFollowersService.class);

    @Autowired
    private MailService mailService;
    @Autowired
    private PageService pageService;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    @Autowired
    private PotentialFollowersJpaRepository potentialFollowersJpaRepository;

    @Autowired
    private ProcessListService processListService;

    @Autowired
    private ProcessResultService processResultService;

    private static final Integer REMOVALS_PER_DAY = 100;
    private static final Integer FOLLOW_REQUESTS_PER_DAY = 100;
    /**
     * This will go back from an element from USERNAMES_FROM_LIST to its correspon
     */
    private static final By LIKE = By.xpath("//*[@aria-label='Like' and @height=24]");
    private static final By UNLIKE = By.xpath("//*[@aria-label='Unlike']");
    private static final By FRIENDSHIP_DROPDOWN = By.xpath("//span[@aria-label ='Following']");
    private static final By REQUESTED_BUTTON = By.xpath("//button[text()='Requested']");
    private static final By FOLLOW_BUTTON_PRIVATE_ACCOUNT = By.xpath("//header/section/div/div/div/div/button[text()='Follow']");
    private static final By FOLLOW_BUTTON_NON_PRIVATE_ACCOUNT = By.xpath("//span/button[text()='Follow']");

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower}
     * that I have already sent a removal request (but not today).
     * It will either confirm the removal, or remove again if the removal was not processed by Instagram.
     */
    public void step2ConfirmRemovals() {
        logger.info("-------------------------CONFIRM REMOVALS------------------------");

        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForConfirmingRemoval(), this::isNumberOfRemovalsPerDayReached, Process.CONFIRM_REMOVING);
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have already sent a follow request,
     * and *MORE* than {@link ProcessListService#NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER} have passed.
     * It will either remove, or follow again if the following was not processed by Instagram.
     */
    public void step3RemoveOrFollow() {
        logger.info("-------------------------REMOVE OR FOLLOW------------------------");

        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForRemoval(), () -> isNumberOfRemovalsPerDayReached()
                        || isNumberOfFollowsPerDayReached(), Process.REMOVING_OR_FOLLOWING);
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have *NOT* sent a follow request,
     * It will just follow.
     */
    public void step4Follow() {
        logger.info("-------------------------FOLLOW PROCESS------------------------");

        List<PotentialFollower> allForFollowing = potentialFollowersJpaRepository.
                getAllForFollowing();
        Integer followRequestSentToday = getFollowRequestSentToday();
        int remainingFollowingsForToday = FOLLOW_REQUESTS_PER_DAY - followRequestSentToday;

        logger.info("All users from DB For Following: " + allForFollowing.size());
        logger.info("Follow request sent today: " + followRequestSentToday);
        logger.info("I still can send follow request today: " + remainingFollowingsForToday);
        if (allForFollowing.size() < remainingFollowingsForToday) {
            logger.info("I do not have enough users to follow in DB so I will start the save potential follower process.");
            processListService.savePotentialFollowersFrom(
                    PageAddress.PE_PLAIURI_ROMANESTI,
                    remainingFollowingsForToday);
        }

        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForFollowing(), this::isNumberOfFollowsPerDayReached, Process.FOLLOWING);
    }

    /**
     * Process all {@link PotentialFollower} for following / confirming following / removing / confirming remove
     *
     * @param potentialFollowersToIterate the {@link List} of {@link PotentialFollower} to iterate over.
     * @param isNumberPerDayReached       the method that checks if we stop iteration or not.
     */
    private void processPotentialFollowers(List<PotentialFollower> potentialFollowersToIterate,
                                           Supplier<Boolean> isNumberPerDayReached, Process processType) {

        ProcessResult processResult = new ProcessResult();
        logger.info("About to open pages and process them: " + potentialFollowersToIterate.size());

        int remaining = getRemaining(potentialFollowersToIterate.size(), processType);

        for (PotentialFollower pF : potentialFollowersToIterate) {
            logger.info("Pages left to iterate ..." + remaining--);
            if (isNumberPerDayReached.get()) break;
            if (!pageService.goToPotentialFollowerPage(pF)) continue;

            switch (getButtonFromPage()) {
                case REQUESTED -> removeOrConfirm(pF, processResult, REQUESTED_BUTTON, "THE PRIVATE USER: " + pF.getId() +
                        " HAS NOT ACCEPTED THE FOLLOW REQUEST SO I JUST REMOVED THE REQUEST");
                case FOLLOWING -> removeOrConfirm(pF, processResult, FRIENDSHIP_DROPDOWN, "I still follow the user, and now I " +
                        "will remove it: " + pF.getId());
                case FOLLOW -> followButtonLogic(pF, processResult);
                case NONE -> {
                    //Aici pur si simplu ajungem foarte des din cauza ca nu se deschide pagina, din cauza la net
                    // sau ceva eroare de la insta, dar a doua oara se deschide. asta cu continue e o solutie temporara
                    logger.info("No button was found on the page of user '{}'(probably the page was not opened). We will try again next run.", pF.getId());
                    continue;
                }
            }
            saveOne(pF);
        }

        processResultService.printResultAndValidate(processType, processResult);
    }

    private Integer getRemaining(Integer totalListSize, Process process) {

        switch (process) {
            case FOLLOWING:
                return FOLLOW_REQUESTS_PER_DAY - getFollowRequestSentToday();
        }
        return totalListSize;
    }

    private void followButtonLogic(PotentialFollower pF, ProcessResult processResult) {
        if (pF.getRemovedFromFollowersAtDate() == null) {
            if (WaitDriver.waitAndGetElement(true, WebDriverUtil.THIS_ACCOUNT_IS_PRIVATE) == null) {
                //click on first picture and like it
                List<WebElement> pagePictures = WaitDriver.waitAndGetElements(false, WebDriverUtil.PAGE_PICTURES);
                pageService.clickByMovingOnElement(pagePictures.get(0));
                pageService.waitForButtonAndClickIt(false, LIKE, UNLIKE);
                pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
            } else {
                if (pF.getFollowRequestSentAtDate() != null) {
                    /* If user is private and if request was already sent and we still have the FOLLOW button */
                    logger.debug("User {} is private, and I will not sent a request again " +
                            "because he may have refused the request and this " +
                            "can be spam / and get blocked or reported. I am setting pageCanBeOpened=false", pF.getId());
                    pF.setPageCanBeOpened(false);
                }
            }
            //Follow
            pageService.waitForButtonAndClickIt(false, FOLLOW_BUTTON_PRIVATE_ACCOUNT, FOLLOW_BUTTON_NON_PRIVATE_ACCOUNT);
            logger.debug("******************************JUST FOLLOWED USER - {}", pF.getId());
            pF.setIsFollowRequested(true);
            pF.setFollowRequestSentAtDate(LocalDate.now());
            processResult.incrementAndGetFollowed();
        } else {
            logger.debug("I confirm that I do not follow anymore the: {}", pF.getId());
            pF.setRemovalConfirmed(true);
            pF.setFollowRequestSentConfirmed(true);
            processResult.incrementAndGetConfirmedRemoved();
        }

    }

    private void removeOrConfirm(PotentialFollower potentialFollower, ProcessResult processResult, By locator, String message) {

        if (followRequestSentLessThanXDaysAgo(potentialFollower.getFollowRequestSentAtDate(), ProcessListService.NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER)) {
            potentialFollower.setFollowRequestSentConfirmed(true);
            logger.debug("I confirm that the request was sent for user: {}", potentialFollower.getId());
            processResult.incrementAndGetConfirmedFollowing();
        } else {
            logger.debug(message);
            pageService.waitForButtonAndClickIt(false, locator);
            pageService.waitForButtonAndClickIt(true, WebDriverUtil.UNFOLLOW_CONFIRMATION);
            potentialFollower.setFollowBackRefused(true);
            potentialFollower.setRemovedFromFollowersAtDate(LocalDate.now());
            potentialFollower.setFollowRequestSentConfirmed(true);
            processResult.incrementAndGetRemoved();
        }
    }

    private boolean followRequestSentLessThanXDaysAgo(LocalDate followRequestSentAtDate, Integer numberOfDays) {

        LocalDate currentTimeMinusXDays = LocalDate.now().minusDays(numberOfDays);
        return followRequestSentAtDate.isAfter(currentTimeMinusXDays);
    }

    private Button getButtonFromPage() {
        if (WaitDriver.waitForElement(true, REQUESTED_BUTTON) != null) {
            return Button.REQUESTED;
        } else if (WaitDriver.waitForElement(true, FRIENDSHIP_DROPDOWN) != null) {
            return Button.FOLLOWING;
        } else if (WaitDriver.waitForElement(true, FOLLOW_BUTTON_PRIVATE_ACCOUNT, FOLLOW_BUTTON_NON_PRIVATE_ACCOUNT) != null) {
            return Button.FOLLOW;
        } else {
            return Button.NONE;
        }
    }

    /**
     * Best to be rolled after refreshingFollowers and removing followers. Basically last step.
     */
    public String analiseFollowRequestResults() {

        RESULT = new StringBuilder();
        /**
         Potential followers to whom I have sent a request and it is confirmed, and more than 3 days have passed*
         */
        List<PotentialFollower> allRequestedUsers = potentialFollowersJpaRepository.getAllRequestedUsers();

        /**
         Potential followers to whom I have sent a request and it is confirmed, and more than 3 days have passed,
         and they are in the follower table*
         */
        List<PotentialFollower> allThatFollowedBack = potentialFollowersJpaRepository.getAllThatFollowedBack();

        printAndAppendToResult(t -> true, "in total", allRequestedUsers, allThatFollowedBack);
        //followers
        printAndAppendToResult(t -> t.getIsAccountPrivate() == true, "private accounts", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getIsAccountPrivate() == false, "NOT private accounts", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowers() < 500 && 0 < t.getFollowers(), "accounts with followers 0-500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowers() < 1000 && 500 < t.getFollowers(), "accounts with followers 500-1000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowers() < 1500 && 1000 < t.getFollowers(), "accounts with followers 1000-1500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowers() < 2000 && 1500 < t.getFollowers(), "accounts with followers 1500-2000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowers() < 2500 && 2000 < t.getFollowers(), "accounts with followers 2000-2500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> 2500 < t.getFollowers(), "accounts with followers 2500-INFINITE", allRequestedUsers, allThatFollowedBack);

        //following
        printAndAppendToResult(t -> t.getFollowing() < 1000 && 500 < t.getFollowing(), "accounts with followings 500-1000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < 1500 && 1000 < t.getFollowing(), "accounts with followings 1000-1500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < 2000 && 1500 < t.getFollowing(), "accounts with followings 1500-2000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < 2500 && 2000 < t.getFollowing(), "accounts with followings 2000-2500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> 2500 < t.getFollowing(), "accounts with followings 2500-INFINITE", allRequestedUsers, allThatFollowedBack);

        //posts
        printAndAppendToResult(t -> t.getPosts() < 50 && 0 < t.getPosts(), "accounts with posts 0-50", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getPosts() < 100 && 50 < t.getPosts(), "accounts with posts 50-100", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getPosts() < 150 && 100 < t.getPosts(), "accounts with posts 100-150", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getPosts() < 250 && 150 < t.getPosts(), "accounts with posts 150-250", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getPosts() < 500 && 250 < t.getPosts(), "accounts with posts 250-500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getPosts() < Integer.MAX_VALUE && 500 < t.getPosts(), "accounts with posts 500-INFINITE", allRequestedUsers, allThatFollowedBack);

        //more following than followers
        printAndAppendToResult(t -> t.getFollowing() > t.getFollowers(), "accounts with more following than followers (like Dora)", allRequestedUsers, allThatFollowedBack);

        //more followers than followings
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers(), "accounts with more followers than following (like raduk_)", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers() && t.getFollowers() < 500, "accounts with more followers than following (like raduk_) and followers between 0-500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers() && t.getFollowers() < 1000 && t.getFollowers() > 500, "accounts with more followers than following (like raduk_) and followers between 500-1000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers() && t.getFollowers() < 1500 && t.getFollowers() > 1000, "accounts with more followers than following (like raduk_) and followers between 1000-1500", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers() && t.getFollowers() < 2000 && t.getFollowers() > 1500, "accounts with more followers than following (like raduk_) and followers between 1500-2000", allRequestedUsers, allThatFollowedBack);
        printAndAppendToResult(t -> t.getFollowing() < t.getFollowers() && t.getFollowers() > 2000, "accounts with more followers than following (like raduk_) and followers between 2000-INFINITE", allRequestedUsers, allThatFollowedBack);

        return RESULT.toString();
    }

    private void printAndAppendToResult(Predicate<PotentialFollower> filter, String
            introText, List<PotentialFollower> total, List<PotentialFollower> partOfTotal) {
        String message = "FOLLOWED " + introText + ": " +
                total.stream().filter(filter).collect(Collectors.toList()).size() +
                " -> THEY FOLLOWED BACK: " +
                partOfTotal.stream().filter(filter).collect(Collectors.toList()).size() + " ( "
                + getPercentage(total.stream().filter(filter).collect(Collectors.toList()),
                partOfTotal.stream().filter(filter).collect(Collectors.toList())) + " % )";
        logger.info(message);
        RESULT.append("\n" + message);
    }

    private Integer getPercentage(List<PotentialFollower> total, List<PotentialFollower> partOfTotal) {
        if (partOfTotal.size() != 0 && total.size() != 0)
            return (100 * partOfTotal.size()) / total.size();
        return 0;
    }

    public boolean isNumberOfRemovalsPerDayReached() {

        if (getRemovalsSentToday() >= REMOVALS_PER_DAY) {
            logger.info("TODAY I ALREADY REACHED MAXIMUM NUMBER OF REMOVAL accounts ");
            return true;
        }
        return false;
    }

    public Integer getRemovalsSentToday() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("removedFromFollowersAtDate"), LocalDate.now()));
        return followRequestSentAtDate.size();
    }

    private boolean isNumberOfFollowsPerDayReached() {

        if (getFollowRequestSentToday() >= FOLLOW_REQUESTS_PER_DAY) {
            logger.info("TODAY I ALREADY REACHED MAXIMUM NUMBER OF FOLLOWED accounts ");
            return true;
        }
        return false;
    }

    public Integer getFollowRequestSentToday() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("followRequestSentAtDate"), LocalDate.now()));
        return followRequestSentAtDate.size();
    }

    public void saveOne(PotentialFollower potentialFollower) {

        potentialFollowersJpaRepository.saveAndFlush(potentialFollower);
    }

    public Optional<PotentialFollower> getOptionalById(String id) {

        return potentialFollowersJpaRepository.findById(id);
    }

    public List<PotentialFollower> getPotentialFollowers(Specification<PotentialFollower> spec) {
        return potentialFollowersJpaRepository.findAll(spec);
    }

    @Transactional
    public void updateFollowRequestSentConfirmed(Set<String> ids) {
        potentialFollowersJpaRepository.updateFollowRequestSentConfirmed(ids);
    }
}
