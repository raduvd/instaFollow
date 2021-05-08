package ro.personal.home.instafollow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.Button;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    //This will go back from an element from USERNAMES_FROM_LIST to its correspon
    private static final By LIKE = By.xpath("//*[@aria-label='Like' and @height=24]");
    private static final By UNLIKE = By.xpath("//*[@aria-label='Unlike']");
    private static final By FOLLOWING_NUMBER = By.xpath("//*[text()=' following']/span");
    private static final By FOLLOWERS_NUMBER = By.xpath("//*[text()=' followers']/span");
    private static final By FOLLOWER_NUMBER = By.xpath("//*[text()=' follower']/span");
    private static final By POSTS_NUMBER = By.xpath("//*[text()=' posts']/span");
    private static final By POST_NUMBER = By.xpath("//*[text()=' post']/span");
    private static final By THIS_ACCOUNT_IS_PRIVATE = By.xpath("//*[text()='This Account is Private']");
    private static final Integer FOLLOW_REQUESTS_PER_DAY = 100;
    private static final Integer REMOVALS_PER_DAY = 100;
    private static final By FRIENDSHIP_DROPDOWN = By.xpath("//span[@aria-label ='Following']");
    private static final By PAGE_UNAVAILABLE = By.xpath("//a[text()='Go back to Instagram.']");
    private static final By REQUESTED_BUTTON = By.xpath("//button[text()='Requested']");

    //TODO sometimes tis logic fails because the page cannot be opened, so just try again if I get this error
    //TODO sometime this locig fails because I get a prompt about "turn on notification" cookies and the close button o fooloow is not found, also in this situation i can reload the page
    //TODO I found users that are not private so maybe check again if the user is really private if I get a failure?
    public void followPotentialFollowers(Integer numerOfPotentialFollowersToFollow) {
        List<PotentialFollower> profilesToFollow = potentialFollowersService.getPotentialFollowerForFollowing();

        System.out.println("POTENTIAL FOLLOWERS TO PROCESS: " + profilesToFollow.size());

        for (PotentialFollower potentialFollower : profilesToFollow) {

            if (isNumberOfFollowsPerDayReached() || numerOfPotentialFollowersToFollow-- <= 0) break;
            //GO to profile page
            String pageAddress = WebDriverUtil.createPageAddress(potentialFollower.getId());
            pageService.goToPage(true, pageAddress);
            if (!isPageAvailable()) {
                System.out.println("The page (" + pageAddress + ") was unavailable, we will move to the next page");
                continue;
            }

            if (!potentialFollower.getIsAccountPrivate()) {
                //click on first picture and like it
                List<WebElement> pagePictures = WaitDriver.waitAndGetElements(true, WebDriverUtil.PAGE_PICTURES);
                pageService.clickByMovingOnElement(pagePictures.get(0));
                pageService.waitForButtonAndClickIt(false, LIKE, UNLIKE);
                pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
            }
            //Follow
            pageService.waitForButtonAndClickIt(false, WebDriverUtil.FOLLOW_BUTTON);
            System.out.println("******************************JUST FOLLOWED USER- " + potentialFollower.getId());
            potentialFollower.setIsFollowRequested(true);
            potentialFollower.setFollowRequestSentAtDate(LocalDate.now());

            potentialFollowersService.saveOne(potentialFollower);
            WaitDriver.sleepForMiliseconds(15000);
        }
    }

    private boolean isPageAvailable() {
        return WaitDriver.waitForElement(true, PAGE_UNAVAILABLE) == null;
    }

    private boolean isNumberOfFollowsPerDayReached() {

        if (getFollowRequestSentToday() >= FOLLOW_REQUESTS_PER_DAY) {
            System.out.println("TODAY I ALREADY REACHED MAXIMUM NUMBER OF FOLLOWED ACCOUNTS ");
            return true;
        }
        return false;
    }

    private Integer getFollowRequestSentToday() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("followRequestSentAtDate"), LocalDate.now()));
        return followRequestSentAtDate.size();
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

    private List<PotentialFollower> getRemovedTodayAndNotConfirmed() {
        Specification<PotentialFollower> removedToday = (r, q, c) -> c.equal(r.get("removedFromFollowersAtDate"), LocalDate.now());
        Specification<PotentialFollower> confirmedIsFalse = (r, q, c) -> c.equal(r.get("removalConfirmed"), false);
        Specification<PotentialFollower> confirmedIsEmpty = (r, q, c) -> c.isNull(r.get("removalConfirmed"));

        return getPotentialFollowers(removedToday.and(confirmedIsEmpty.or(confirmedIsFalse)));
    }

    public void confirmRemovedUsers(Integer numberOfIterations) {

        IntStream.range(0, numberOfIterations).forEach(i -> {

            List<PotentialFollower> removedFromFollowersAtDate = getRemovedTodayAndNotConfirmed();
            System.out.println("Users that I removed today and the removal is not confirmed : " + removedFromFollowersAtDate.size());
            System.out.println("We are about to go to each page individually and confirm.");

            for (PotentialFollower potentialFollower : removedFromFollowersAtDate) {
                String pageAddress = WebDriverUtil.createPageAddress(potentialFollower.getId());
                pageService.goToPage(true, pageAddress);
                if (WaitDriver.waitForElement(true, WebDriverUtil.FOLLOW_BUTTON) != null) {
                    System.out.println("I confirm that I do not follow anymore the:  " + pageAddress);
                    potentialFollower.setRemovalConfirmed(true);
                    potentialFollowersService.saveOne(potentialFollower);
                } else {
                    System.out.println("I still follow the page, and now I will remove it again: " + pageAddress);
                    pageService.waitForButtonAndClickIt(false, FRIENDSHIP_DROPDOWN);
                    pageService.waitForButtonAndClickIt(true, WebDriverUtil.UNFOLLOW_CONFIRMATION);
                }
            }
        });
    }

    public boolean followUserOrConfirmFollowing(PotentialFollower potentialFollower) {
        if (potentialFollowersService.isNumberOfFollowsPerDayReached()
                || potentialFollower.getIsRejectedDueToValidation() == true
                || potentialFollower.getFollowBackRefused() == true
                || potentialFollower.getRemovedFromFollowersAtDate() != null
                || potentialFollower.getFollowRequestSentConfirmed() == true
                || potentialFollower.getRemovalConfirmed() == true)
            return false;
        return true;
    }

    public boolean removeUserOrConfirmRemoval(PotentialFollower potentialFollower) {

        if (potentialFollowersService.isNumberOfRemovalsPerDayReached()
                || potentialFollower.getIsFollowRequested() == false
                || potentialFollower.getFollowRequestSentAtDate() == null
                || potentialFollower.getFollowRequestSentAtDate().
                isAfter(LocalDate.now().minusDays(ProcessListService.NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER))
                || potentialFollower.getRemovalConfirmed() == true)
            return false;
        return true;
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have already sent a removal request (but not today).
     * It will either confirm the removal, or remove again if the removal was not processed by Instagram.
     */
    //TODO if I want to reduce the number of page accesed per day, I can leave the private account out here. They do not necesarryly need removal? or confirmation?
    public void step1ConfirmRemovals() {
//TODO THis method It will either confirm the removal, or remove again if the removal was not processed by Instagram,
// so just check the logs to contain only this kind of prints
        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForConfirmingRemoval(), () -> isNumberOfRemovalsPerDayReached());
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have already sent a follow request,
     * and *NO* more than {@link ProcessListService#NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER} have passed.
     * It will either confirm the following, or send again if the following was not processed by Instagram.
     */
    public void step2ConfirmFollowing() {
//TODO THis method It will either confirm the following, or send again if the following was not processed by Instagram,
// so just check the logs to contain only this
        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForConfirmingFollowing(), () -> isNumberOfFollowsPerDayReached());
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have already sent a follow request,
     * and *MORE* than {@link ProcessListService#NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER} have passed.
     * It will either remove, or follow again if the following was not processed by Instagram.
     */
    //TODO check if this logic will remove users that are not private, with other words, check if this logick will replace the logic where we iterate the following list and remove users

    public void step3RemoveOrFollow() {
//TODO THis method It will either remove, or send again a following if the following was not processed by Instagram,
// so just check the logs to contain only this
        processPotentialFollowers(
                potentialFollowersJpaRepository.
                        getAllForRemoval(), () -> isNumberOfRemovalsPerDayReached()
                        || isNumberOfFollowsPerDayReached());
    }

    /**
     * This method will go trough a {@link List} of {@link PotentialFollower} that I have *NOT* sent a follow request,
     * It will just follow.
     */
    public void step4Follow() {
//TODO THis method  It will just follow.
// so just check the logs to contain only this
        List<PotentialFollower> allForFollowing = potentialFollowersJpaRepository.
                getAllForFollowing();
        if (allForFollowing.size() < ProcessListService.NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER - getFollowRequestSentToday())
            //TODO when I automate the process, replace this error with logic that will add new potential followers
            throw new RuntimeException("NOT ENOUGH USERS TO PROCESS. Better add new ones.");

        processPotentialFollowers(
                allForFollowing, () -> isNumberOfFollowsPerDayReached());
    }

    /**
     * Process all {@link PotentialFollower} for following / confirming following / removing / confirming remove
     *
     * @param potentialFollowersToIterate the {@link List} of {@link PotentialFollower} to iterate over.
     * @param isNumberPerDayReached       the method that checks if we stop iteration or not.
     */
    //TODO update the result analisis logic to take only confirmed
    //TODO after a month or two, simplyfy this and remove duplicate logic in other mehtods, before removing try to add stuff that are missing here but are present in the removable method
    //TODO for the next week go trough all the code in debug
    //TODO check how many of the request are not being processed, and maybe wait more and see if it improoves, or combine them, a follow then a removal and so on
    // TODO after a month or a week check in the DB- all should have confirmedRemoval = true and confirmedFoloowing = true, if not the logic is not good
    //TODO add some logs to check how many did you followed/removed/re-removed/re-followed/confirmed-Remmoval/confirmed-following,
    //      make an object that gets populated with this info,
    //      gets populated in this method and gets returned
    //      adn with the returned object throw error in each method that is calling this method if not expected things get processed.
    private void processPotentialFollowers(List<PotentialFollower> potentialFollowersToIterate, Supplier<Boolean> isNumberPerDayReached) {

        System.out.println("About to open pages: " + potentialFollowersToIterate.size());
        int counter = potentialFollowersToIterate.size();

        for (PotentialFollower pF : potentialFollowersToIterate) {
            System.out.println("Pages left to iterate ..." + counter--);
            if (isNumberPerDayReached.get()) break;

            //TODO add true to goToPage method if I run it without debug
            String pageAddress = WebDriverUtil.createPageAddress(pF.getId());
            if (!pageService.goToPage(true, pageAddress))
                //TODO pages that do not oppen eg: https://www.instagram.com/bradcalin https://www.instagram.com/jasminka_2021
                //          and the follow request is not sent, just remove them from DB, if the follow request is already sent, this means that the page opend once so just try again
               //           or maybe I just need a backslash? try this before going deeper
                //          DO NOT just leave it like this because it seems that is NOT something from instagram, every time it does not work
               //           add it to the goToPage method?
                continue;

            switch (getButtonFromPage()) {
                case REQUESTED -> removeOrConfirm(pF, REQUESTED_BUTTON, "THE PRIVATE USER: " + pF.getId() +
                        " HAS NOT ACCEPTED THE FOLLOW REQUEST SO I JUST REMOVED THE REQUEST");
                case FOLLOWING -> removeOrConfirm(pF, FRIENDSHIP_DROPDOWN, "I still follow the user, and now I " +
                        "will remove it: " + pF.getId());
                case FOLLOW -> {
                    if (pF.getRemovedFromFollowersAtDate() == null) {
                        if (!pF.getIsAccountPrivate()) {
                            //click on first picture and like it
                            List<WebElement> pagePictures = WaitDriver.waitAndGetElements(true, WebDriverUtil.PAGE_PICTURES);
                            pageService.clickByMovingOnElement(pagePictures.get(0));
                            pageService.waitForButtonAndClickIt(false, LIKE, UNLIKE);
                            pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
                        }
                        //Follow
                        //TODO atentie mare, un cont privat are 5 private buttons pe pagina, trebuie sa gasesc doar pe cel al paginii pe care usnt!!
                        pageService.waitForButtonAndClickIt(false, WebDriverUtil.FOLLOW_BUTTON);
                        System.out.println("******************************JUST FOLLOWED USER- " + pF.getId());
                        pF.setIsFollowRequested(true);
                        pF.setFollowRequestSentAtDate(LocalDate.now());
                    } else {
                        System.out.println("I confirm that I do not follow anymore the:  " + pF.getId());
                        pF.setRemovalConfirmed(true);
                        pF.setFollowRequestSentConfirmed(true);
                    }
                }
                case NONE -> throw new RuntimeException("No button was found on the page: " + pageAddress);
            }
            saveOne(pF);
        }
    }

    private void removeOrConfirm(PotentialFollower potentialFollower, By locator, String message) {
        if (followRequestSentLessThan2DaysAgo(potentialFollower.getFollowRequestSentAtDate())) {
            potentialFollower.setFollowRequestSentConfirmed(true);
            System.out.println("I confirm that the request was sent for user: " + potentialFollower.getId());
        } else {
            System.out.println(message);
            pageService.waitForButtonAndClickIt(false, locator);
            pageService.waitForButtonAndClickIt(true, WebDriverUtil.UNFOLLOW_CONFIRMATION);
            potentialFollower.setFollowBackRefused(true);
            potentialFollower.setRemovedFromFollowersAtDate(LocalDate.now());
            potentialFollower.setFollowRequestSentConfirmed(true);
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

        }
        //TODO atentie mare, un cont privat are 5 private buttons pe pagina, trebuie sa gasesc doar pe cel al paginii pe care usnt!!
        else if (WaitDriver.waitForElement(true, WebDriverUtil.FOLLOW_BUTTON) != null) {
            return Button.FOLLOW;
        } else {
            return Button.NONE;
        }
    }

    /**
     * Best to be rolled after refreshingFollowers and removing followers.
     */
    public void analiseFollowRequestResults() {

        //Potential followers to whom I have sent a request, more than 2 days have passed
        List<PotentialFollower> allRequested = potentialFollowersJpaRepository.getAllRequestedUsers();

        //Potential followers to whom I have sent a request, more than 2 days have passed and they FOLLOWED BACK
        List<PotentialFollower> usersThatFollowedBack = potentialFollowersJpaRepository.getRequestedUsersWhoFollowedBack();

        //Potential followers to whom I have sent a request, more than 2 days have passed and they DID *NOT* FOLLOWED BACK
        List<PotentialFollower> usersThatDidNotFollowedBack = allRequested.stream().
                filter(u -> !usersThatFollowedBack.contains(u)).collect(Collectors.toList());

        System.out.println("allRequested " + allRequested.size());
        System.out.println("usersThatFollowedBack " + usersThatFollowedBack.size());
        System.out.println("usersThatDidNotFollowedBack " + usersThatDidNotFollowedBack.size());

        List<Followers> followersList = followerService.getAllByIsNoMore(false);

        print(t -> true, "-", allRequested, usersThatFollowedBack);

        //followers
        print(t -> t.getIsAccountPrivate() == true, "Private", allRequested, usersThatFollowedBack);
        print(t -> t.getIsAccountPrivate() == false, "NOT Private", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 500 && 0 < t.getFollowers(), "Accounts with followers 0-500", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 1000 && 500 < t.getFollowers(), "Accounts with followers 500-1000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 1500 && 1000 < t.getFollowers(), "Accounts with followers 1000-1500", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 2000 && 1500 < t.getFollowers(), "Accounts with followers 1500-2000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 2500 && 2000 < t.getFollowers(), "Accounts with followers 2000-2500", allRequested, usersThatFollowedBack);

        //following
        print(t -> t.getFollowing() < 1000 && 500 < t.getFollowing(), "Accounts with followings 500-1000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowing() < 1500 && 1000 < t.getFollowing(), "Accounts with followings 1000-1500", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowing() < 2000 && 1500 < t.getFollowing(), "Accounts with followings 1500-2000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowing() < 2500 && 2000 < t.getFollowing(), "Accounts with followings 2000-2500", allRequested, usersThatFollowedBack);

        //posts
        print(t -> t.getPosts() < 50 && 0 < t.getPosts(), "Accounts with posts 0-50", allRequested, usersThatFollowedBack);
        print(t -> t.getPosts() < 100 && 50 < t.getPosts(), "Accounts with posts 50-100", allRequested, usersThatFollowedBack);
        print(t -> t.getPosts() < 150 && 100 < t.getPosts(), "Accounts with posts 100-150", allRequested, usersThatFollowedBack);
        print(t -> t.getPosts() < 250 && 150 < t.getPosts(), "Accounts with posts 150-250", allRequested, usersThatFollowedBack);
        print(t -> t.getPosts() < 500 && 250 < t.getPosts(), "Accounts with posts 250-500", allRequested, usersThatFollowedBack);
        print(t -> t.getPosts() < Integer.MAX_VALUE && 500 < t.getPosts(), "Accounts with posts 500-INFINITE", allRequested, usersThatFollowedBack);

        //more following than followers
        print(t -> t.getFollowing() > t.getFollowers(), "Accounts with more following than followers (like raduc_)", allRequested, usersThatFollowedBack);

        //more followers than followings
        print(t -> t.getFollowing() < t.getFollowers(), "Accounts with more followers than following", allRequested, usersThatFollowedBack);
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

    public void saveOne(PotentialFollower potentialFollower) {

        potentialFollowersJpaRepository.saveAndFlush(potentialFollower);
    }

    public Optional<PotentialFollower> getOptionalById(String id) {

        return potentialFollowersJpaRepository.findById(id);
    }

    public List<PotentialFollower> getPotentialFollowers(Specification<PotentialFollower> spec) {
        return potentialFollowersJpaRepository.findAll(spec);
    }

    private Specification<PotentialFollower> getPotentialFollowerWithIsFollowRequested() {
        return (root, query, cb) -> cb.equal(root.get("isFollowRequested"), false);
    }

    private Specification<PotentialFollower> getPotentialFollowerWithIsRejectedDueToValidation() {
        return (root, query, cb) -> cb.equal(root.get("isRejectedDueToValidation"), false);
    }

    private Specification<PotentialFollower> getPotentialFollowerWithNotPrivateAccount() {
        return (root, query, cb) -> cb.equal(root.get("isAccountPrivate"), false);
    }

    private Specification<PotentialFollower> getAllWithNoFollowRequestConfirmation() {

        Specification<PotentialFollower> followRequestSentConfirmed = (root, query, cb) ->
                cb.equal(root.get("followRequestSentConfirmed"), false);
        Specification<PotentialFollower> followRequestSentIsEmpty = (root, query, cb) ->
                cb.isNull(root.get("followRequestSentConfirmed"));

        return followRequestSentConfirmed.or(followRequestSentIsEmpty);
    }

    public List<PotentialFollower> getPotentialFollowerForFollowing() {
        return getPotentialFollowers(getPotentialFollowerWithIsFollowRequested().and(getPotentialFollowerWithIsRejectedDueToValidation()));
    }
}
