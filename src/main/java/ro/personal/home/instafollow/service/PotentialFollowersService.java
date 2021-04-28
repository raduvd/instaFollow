package ro.personal.home.instafollow.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

    //This will go back from an element from USERNAMES_FROM_LIST to its correspon
    private static final By LIKE = By.xpath("//*[@aria-label='Like' and @height=24]");
    private static final By UNLIKE = By.xpath("//*[@aria-label='Unlike']");
    private static final By FOLLOW = By.xpath("//button[text()='Follow']");
    private static final By FOLLOWING_NUMBER = By.xpath("//*[text()=' following']/span");
    private static final By FOLLOWERS_NUMBER = By.xpath("//*[text()=' followers']/span");
    private static final By FOLLOWER_NUMBER = By.xpath("//*[text()=' follower']/span");
    private static final By POSTS_NUMBER = By.xpath("//*[text()=' posts']/span");
    private static final By POST_NUMBER = By.xpath("//*[text()=' post']/span");
    private static final By THIS_ACCOUNT_IS_PRIVATE = By.xpath("//*[text()='This Account is Private']");
    private static final Integer FOLLOW_REQUESTS_PER_DAY = 50;
    private static final Integer REMOVALS_PER_DAY = 50;

    public void followPotentialFollowers(Integer numerOfPotentialFollowersToFollow) {
        List<PotentialFollower> profilesToFollow = potentialFollowersService.getPotentialFollowerForFollowing();

        System.out.println("POTENTIAL FOLLOWERS TO PROCESS: " + profilesToFollow.size());

        for (PotentialFollower potentialFollower : profilesToFollow) {

            if (isNumberOfFollowsPerDayReached() || numerOfPotentialFollowersToFollow-- <= 0) break;
            //GO to profile page
            pageService.goToPage(WebDriverUtil.createPageAddress(potentialFollower.getId()));

            if (!potentialFollower.getIsAccountPrivate()) {
                //click on first picture and like it
                List<WebElement> pagePictures = WaitDriver.waitAndGetElements(true, WebDriverUtil.PAGE_PICTURES);
                pageService.clickByMovingOnElement(pagePictures.get(0));
                pageService.waitForButtonAndClickIt(false, LIKE, UNLIKE);
                pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
            }
            //Follow
            pageService.waitForButtonAndClickIt(false, FOLLOW);
            System.out.println("******************************JUST FOLLOWED USER- " + potentialFollower.getId());
            potentialFollower.setIsFollowRequested(true);
            potentialFollower.setFollowRequestSentAtDate(LocalDate.now());

            potentialFollowersService.saveOne(potentialFollower);
        }

        WebDriverUtil.printResultInConsoleLog(pageService.getResult());
    }

    private boolean isNumberOfFollowsPerDayReached() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("followRequestSentAtDate"), LocalDate.now()));

        if (followRequestSentAtDate.size() >= FOLLOW_REQUESTS_PER_DAY) {
            pageService.getResult().getMessages().add("TODAY I ALREADY REACHED MAXIMUM NUMBER OF FOLLOWED ACCOUNTS ");
            return true;
        }
        return false;
    }

    public boolean isNumberOfRemovalsPerDayReached() {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("removedFromFollowersAtDate"), LocalDate.now()));

        if (followRequestSentAtDate.size() >= REMOVALS_PER_DAY) {
            pageService.getResult().getMessages().add("TODAY I ALREADY REACHED MAXIMUM NUMBER OF REMOVAL ACCOUNTS ");
            return true;
        }
        return false;
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
        List<PotentialFollower> usersThatDidNotFollowedBack = allRequested.stream().filter(u -> !usersThatFollowedBack.contains(u)).collect(Collectors.toList());

        System.out.println("allRequested " + allRequested.size());
        System.out.println("usersThatFollowedBack " + usersThatFollowedBack.size());
        System.out.println("usersThatDidNotFollowedBack " + usersThatDidNotFollowedBack.size());

        List<Followers> followersList = followerService.getAllByIsNoMore(false);

        print(t -> true, "-", allRequested, usersThatFollowedBack);

        //followings
        print(t -> t.getIsAccountPrivate() == true, "Private", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 500 && 0 < t.getFollowers(), "Accounts with followers 0-500", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 1000 && 500 < t.getFollowers(), "Accounts with followers 500-1000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 1500 && 1000 < t.getFollowers(), "Accounts with followers 1000-1500", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 2000 && 1500 < t.getFollowers(), "Accounts with followers 1500-2000", allRequested, usersThatFollowedBack);
        print(t -> t.getFollowers() < 2500 && 2000 < t.getFollowers(), "Accounts with followers 2000-2500", allRequested, usersThatFollowedBack);

        //followers
        print(t -> t.getFollowing() < 500 && 0 < t.getFollowing(), "Accounts with followings 0-500", allRequested, usersThatFollowedBack);
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

    private void print(Predicate<PotentialFollower> filter, String introText, List<PotentialFollower> total, List<PotentialFollower> partOfTotal) {
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

    public List<PotentialFollower> getPotentialFollowerForFollowing() {
        return getPotentialFollowers(getPotentialFollowerWithIsFollowRequested().and(getPotentialFollowerWithIsRejectedDueToValidation()));
    }
}
