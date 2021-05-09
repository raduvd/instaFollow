package ro.personal.home.instafollow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.Button;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.model.Result;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
public class PotentialFollowersService {

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
    private ResultService resultService;

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
        System.out.println("-------------------------CONFIRM REMOVALS------------------------");

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
        System.out.println("-------------------------REMOVE OR FOLLOW------------------------");

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
        System.out.println("-------------------------FOLLOW PROCESS------------------------");

        List<PotentialFollower> allForFollowing = potentialFollowersJpaRepository.
                getAllForFollowing();

        int remainingFollowingsForToday = FOLLOW_REQUESTS_PER_DAY - getFollowRequestSentToday();
        if (allForFollowing.size() < remainingFollowingsForToday)
            processListService.savePotentialFollowersFrom(PageAddress.PE_PLAIURI_ROMANESTI, remainingFollowingsForToday);

        processPotentialFollowers(
                allForFollowing, this::isNumberOfFollowsPerDayReached, Process.FOLLOWING);
    }

    /**
     * Process all {@link PotentialFollower} for following / confirming following / removing / confirming remove
     *
     * @param potentialFollowersToIterate the {@link List} of {@link PotentialFollower} to iterate over.
     * @param isNumberPerDayReached       the method that checks if we stop iteration or not.
     */
    private void processPotentialFollowers(List<PotentialFollower> potentialFollowersToIterate,
                                           Supplier<Boolean> isNumberPerDayReached, Process processType) {


        Result processResult = new Result();
        System.out.println("About to open pages and process them: " + potentialFollowersToIterate.size());

        int remaining = potentialFollowersToIterate.size();

        for (PotentialFollower pF : potentialFollowersToIterate) {
            System.out.println("Pages left to iterate ..." + remaining--);
            if (isNumberPerDayReached.get()) break;
            if (!pageService.goToPotentialFollowerPage(pF)) continue;

            switch (getButtonFromPage()) {
                case REQUESTED -> removeOrConfirm(pF, processResult, REQUESTED_BUTTON, "THE PRIVATE USER: " + pF.getId() +
                        " HAS NOT ACCEPTED THE FOLLOW REQUEST SO I JUST REMOVED THE REQUEST");
                case FOLLOWING -> removeOrConfirm(pF, processResult, FRIENDSHIP_DROPDOWN, "I still follow the user, and now I " +
                        "will remove it: " + pF.getId());
                case FOLLOW -> followButtonLogic(pF, processResult);
                case NONE -> throw new RuntimeException("No button was found on the page");
            }
            saveOne(pF);
        }

        resultService.printResultAndConfirmProcessType(processType, processResult);
    }

    private void followButtonLogic(PotentialFollower pF, Result processResult) {
        if (pF.getRemovedFromFollowersAtDate() == null) {

            if (WaitDriver.waitAndGetElement(true, WebDriverUtil.THIS_ACCOUNT_IS_PRIVATE) == null) {
                //click on first picture and like it
                List<WebElement> pagePictures = WaitDriver.waitAndGetElements(false, WebDriverUtil.PAGE_PICTURES);
                pageService.clickByMovingOnElement(pagePictures.get(0));
                pageService.waitForButtonAndClickIt(false, LIKE, UNLIKE);
                pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
            }
            //Follow
            pageService.waitForButtonAndClickIt(false, FOLLOW_BUTTON_PRIVATE_ACCOUNT, FOLLOW_BUTTON_NON_PRIVATE_ACCOUNT);
            System.out.println("******************************JUST FOLLOWED USER- " + pF.getId());
            pF.setIsFollowRequested(true);
            pF.setFollowRequestSentAtDate(LocalDate.now());
            processResult.getFollowed().getAndIncrement();
        } else {
            System.out.println("I confirm that I do not follow anymore the:  " + pF.getId());
            pF.setRemovalConfirmed(true);
            pF.setFollowRequestSentConfirmed(true);
            processResult.getConfirmedRemoved().getAndIncrement();
        }
    }

    private void removeOrConfirm(PotentialFollower potentialFollower, Result processResult, By locator, String message) {

        if (followRequestSentLessThan2DaysAgo(potentialFollower.getFollowRequestSentAtDate())) {
            potentialFollower.setFollowRequestSentConfirmed(true);
            System.out.println("I confirm that the request was sent for user: " + potentialFollower.getId());
            processResult.getConfirmedFollowing().getAndIncrement();
        } else {
            System.out.println(message);
            pageService.waitForButtonAndClickIt(false, locator);
            pageService.waitForButtonAndClickIt(true, WebDriverUtil.UNFOLLOW_CONFIRMATION);
            potentialFollower.setFollowBackRefused(true);
            potentialFollower.setRemovedFromFollowersAtDate(LocalDate.now());
            potentialFollower.setFollowRequestSentConfirmed(true);
            processResult.getRemoved().getAndIncrement();
        }
    }

    private boolean followRequestSentLessThan2DaysAgo(LocalDate followRequestSentAtDate) {

        LocalDate currentTimeMinusXDays = LocalDate.now().minusDays(ProcessListService.NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER);
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
     * Best to be rolled after refreshingFollowers and removing followers.
     */
    public void analiseFollowRequestResults() {

//        //Potential followers to whom I have sent a request, more than 2 days have passed
//        List<PotentialFollower> allRequested = potentialFollowersJpaRepository.getAllRequestedUsers();
//
//        //Potential followers to whom I have sent a request, more than 2 days have passed and they FOLLOWED BACK
//        List<PotentialFollower> usersThatFollowedBack = potentialFollowersJpaRepository.getRequestedUsersWhoFollowedBack();
//
//        //Potential followers to whom I have sent a request, more than 2 days have passed and they DID *NOT* FOLLOWED BACK
//        List<PotentialFollower> usersThatDidNotFollowedBack = allRequested.stream().
//                filter(u -> !usersThatFollowedBack.contains(u)).collect(Collectors.toList());
//
//        System.out.println("allRequested " + allRequested.size());
//        System.out.println("usersThatFollowedBack " + usersThatFollowedBack.size());
//        System.out.println("usersThatDidNotFollowedBack " + usersThatDidNotFollowedBack.size());
//
//        List<Followers> followersList = followerService.getAllByIsNoMore(false);
//
//        print(t -> true, "-", allRequested, usersThatFollowedBack);
//
//        //followers
//        print(t -> t.getIsAccountPrivate() == true, "Private", allRequested, usersThatFollowedBack);
//        print(t -> t.getIsAccountPrivate() == false, "NOT Private", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowers() < 500 && 0 < t.getFollowers(), "Accounts with followers 0-500", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowers() < 1000 && 500 < t.getFollowers(), "Accounts with followers 500-1000", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowers() < 1500 && 1000 < t.getFollowers(), "Accounts with followers 1000-1500", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowers() < 2000 && 1500 < t.getFollowers(), "Accounts with followers 1500-2000", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowers() < 2500 && 2000 < t.getFollowers(), "Accounts with followers 2000-2500", allRequested, usersThatFollowedBack);
//
//        //following
//        print(t -> t.getFollowing() < 1000 && 500 < t.getFollowing(), "Accounts with followings 500-1000", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowing() < 1500 && 1000 < t.getFollowing(), "Accounts with followings 1000-1500", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowing() < 2000 && 1500 < t.getFollowing(), "Accounts with followings 1500-2000", allRequested, usersThatFollowedBack);
//        print(t -> t.getFollowing() < 2500 && 2000 < t.getFollowing(), "Accounts with followings 2000-2500", allRequested, usersThatFollowedBack);
//
//        //posts
//        print(t -> t.getPosts() < 50 && 0 < t.getPosts(), "Accounts with posts 0-50", allRequested, usersThatFollowedBack);
//        print(t -> t.getPosts() < 100 && 50 < t.getPosts(), "Accounts with posts 50-100", allRequested, usersThatFollowedBack);
//        print(t -> t.getPosts() < 150 && 100 < t.getPosts(), "Accounts with posts 100-150", allRequested, usersThatFollowedBack);
//        print(t -> t.getPosts() < 250 && 150 < t.getPosts(), "Accounts with posts 150-250", allRequested, usersThatFollowedBack);
//        print(t -> t.getPosts() < 500 && 250 < t.getPosts(), "Accounts with posts 250-500", allRequested, usersThatFollowedBack);
//        print(t -> t.getPosts() < Integer.MAX_VALUE && 500 < t.getPosts(), "Accounts with posts 500-INFINITE", allRequested, usersThatFollowedBack);
//
//        //more following than followers
//        print(t -> t.getFollowing() > t.getFollowers(), "Accounts with more following than followers (like raduc_)", allRequested, usersThatFollowedBack);
//
//        //more followers than followings
//        print(t -> t.getFollowing() < t.getFollowers(), "Accounts with more followers than following", allRequested, usersThatFollowedBack);
    }

    private void print(Predicate<PotentialFollower> filter, String
            introText, List<PotentialFollower> total, List<PotentialFollower> partOfTotal) {
        System.out.println("Followed " + introText + "--" + total.stream().filter(filter).collect(Collectors.toList()).size() + " -> FOLLOW-BACK " +
                partOfTotal.stream().filter(filter).collect(Collectors.toList()).size() + "(" + getPercentage(total.stream().filter(filter).collect(Collectors.toList()),
                partOfTotal.stream().filter(filter).collect(Collectors.toList())) + ")");
    }

    private Integer getPercentage(List<PotentialFollower> total, List<PotentialFollower> partOfTotal) {
        if (partOfTotal.size() != 0 && total.size() != 0)
            return (100 * partOfTotal.size()) / total.size();
        return 0;
    }

    private boolean isNumberOfFollowsPerDayReached() {

        if (getFollowRequestSentToday() >= FOLLOW_REQUESTS_PER_DAY) {
            System.out.println("TODAY I ALREADY REACHED MAXIMUM NUMBER OF FOLLOWED ACCOUNTS ");
            return true;
        }
        return false;
    }

    public boolean isNumberOfRemovalsPerDayReached() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("removedFromFollowersAtDate"), LocalDate.now()));

        if (followRequestSentAtDate.size() >= REMOVALS_PER_DAY) {
            System.out.println("TODAY I ALREADY REACHED MAXIMUM NUMBER OF REMOVAL ACCOUNTS ");
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
}
