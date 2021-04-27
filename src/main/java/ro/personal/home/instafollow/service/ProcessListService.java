package ro.personal.home.instafollow.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.ListType;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.model.ProcessedPicture;
import ro.personal.home.instafollow.persistance.repository.ProcessedPictureJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.AppWebDriver;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ProcessListService {

    @Autowired
    private PageService pageService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    @Autowired
    private ProcessedPictureJpaRepository processedPictureJpaRepository;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private FollowingService followingService;

    /**
     * Ex: If more that 2 days pass and the user does not follow back, we remove him.
     */
    private static Integer NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER = 2;

    private static final By FOLLOWING_NUMBER = By.xpath("//div/span[text()=' following']/span");
    private static final By FOLLOWERS_NUMBER = By.xpath("//div/span[text()=' followers']/span");
    private static final By FOLLOWER_NUMBER = By.xpath("//div/span[text()=' follower']/span");
    private static final By POSTS_NUMBER = By.xpath("//div/span[text()=' posts']/span");
    private static final By POST_NUMBER = By.xpath("//div/span[text()=' post']/span");


    private static final By OTHERS_LIST = By.xpath("//a[text()=' others']");
    private static final By OTHERS_LIST_SIZE = By.xpath("//a[text()=' others']/span");
    private static final By LIKES_LIST_USERNAMES = By.xpath("//button[text()='Follow']/../../div[2]/div/div/span/a");
    private static final By LIST_USERNAME_LINKS = By.xpath("//a[@title and @href and @tabindex]");
    private static final By MY_FOLLOWERS_MAIN_BUTTON = By.xpath("//a[@href='/raduvd/followers/']");
    private static final By MY_FOLLOWERS_NUMBER = By.xpath("//a[@href='/raduvd/followers/']/span");
    private static final By MY_FOLLOWING_MAIN_BUTTON = By.xpath("//a[@href='/raduvd/following/']");
    private static final By MY_FOLLOWING_NUMBER = By.xpath("//a[@href='/raduvd/following/']/span");
    private static final By PRIVATE_ACCOUNT_TEXT = By.xpath("//*[text()='The Account is Private']");
    //navigate from the link of an element backwards to its button (Follow, Remove or any)
    private static final By LIST_ELEMENT_BUTTON = By.xpath("../../../../../div[2]/button");
    private static final By UNFOLLOW_CONFIRMATION = By.xpath("//button[text() = 'Unfollow']");

    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS = 2500;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS = 2500;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS = 100;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS = 100;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS = 10;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_DIFERENCE_BETWEEN_FOLLOWERS_MINUS_FOLLOWING = 200;

    public void tempCheckIsAccountPrivate() {
    }

    /**
     * @param pageAddresses           Ex: 'Pe Plaiuri Romanesti' profile page, from where I will get a the last post and check the users that have liked it.
     * @param pictureIndexFromProfile the profile has a multitude of picture, this index will chose one of these pictures. Usually this is zero always.
     */
    public void savePotentialFollowersFrom(List<PageAddress> pageAddresses, Integer pictureIndexFromProfile) {

        for (PageAddress pageAddress : pageAddresses) {
            //Go to page/profile
            pageService.goToPage(pageAddress.getLinkToPage());

            //Get the profile pictures
            List<WebElement> pagePictures = WaitDriver.waitAndGetElements(false, WebDriverUtil.PAGE_PICTURES);

            //Get the picture according to pictureIndexFromProfile
            validatePictureIndex(pagePictures, pictureIndexFromProfile);
            WebElement pictureFromProfile = pagePictures.get(pictureIndexFromProfile);
            ProcessedPicture processedPicture = getOrSavePictureEntity(pictureFromProfile, pageAddress);
            if (isPictureInvalid(processedPicture)) {
                pageService.getResult().getMessages().add(
                        "Picture index " + pictureIndexFromProfile + " from profile " +
                                pageAddress + "was not processed because it is already processed");
                continue;
            }
            pageService.clickByMovingOnElement(pictureFromProfile);

            //OPEN LIST OF profiles that have LIKEd the picture
            pageService.waitForButtonAndClickIt(false, OTHERS_LIST);
            //process the list and save in DB the usernames
            Integer listSize = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, OTHERS_LIST_SIZE);
            processIgList(LIKES_LIST_USERNAMES, ListType.LIKE_LIST, listSize);

            processedPicture.setIsProcessed(true);
            processedPictureJpaRepository.saveAndFlush(processedPicture);

            WebDriverUtil.printResultInConsoleLog(pageService.getResult());
        }
    }

    public void processFollowingListAndRemoveAccountsThatDoNotFollowBack() {
        pageService.goToPage(PageAddress.INSTAGRAM_MY_ACCOUNT.getLinkToPage());
        pageService.waitForButtonAndClickIt(false, MY_FOLLOWING_MAIN_BUTTON);

        Integer myFollowingNumber = (Integer) pageService.
                getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, MY_FOLLOWING_NUMBER);

        Set<String> removedUsers = processIgList(LIST_USERNAME_LINKS, ListType.FOLLOWING_LIST, myFollowingNumber);

        System.out.println("REMOVED " + removedUsers.size() + " USERS. I SHOULD HAVE NOW: " + (myFollowingNumber - removedUsers.size()));
    }

    /**
     * Gets all followers from list and saves them in DB (isNoMore=false).
     * The old followers that are not found here are set to isNoMore = true, if process is without error.
     */
    public void refreshFollowers() {

        pageService.goToPage(PageAddress.INSTAGRAM_MY_ACCOUNT.getLinkToPage());

        Integer myFollowersNumber = (Integer) pageService.
                getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, MY_FOLLOWERS_NUMBER);

        pageService.waitForButtonAndClickIt(false, MY_FOLLOWERS_MAIN_BUTTON);

        Set<String> followersFoundInWebDriver = processIgList(LIST_USERNAME_LINKS, ListType.FOLLOWER_LIST, myFollowersNumber);

        if (followersFoundInWebDriver.size() == myFollowersNumber) {
            followingService.setIsNoMore();
            List<Followers> followers = followerService.createFollowers(new ArrayList<>(followersFoundInWebDriver));
            System.out.println("--------------------------WE ADDED IN DB FOLLOWERS: " + followers.size());
            System.out.println("My Followers: " + followers);
        } else {

            throw new RuntimeException("The followers Found In Web Driver(" + followersFoundInWebDriver.size() + ") are not equal with the followers number from my page(" + myFollowersNumber + "). " +
                    "This means that the logic is wrong.");
        }
    }

    public boolean isFollowerValid(PotentialFollower potentialFollower) {

        boolean isUserAFollower = followingService.getFollowingJpaRepository().findById(potentialFollower.getId()).isPresent();
        boolean isUserAPotentialFollower = potentialFollowersService.getOptionalById(potentialFollower.getId()).isPresent();
        if (isUserAFollower || isUserAPotentialFollower) return false;

        boolean isAccountPrivate = null != WaitDriver.waitAndGetElement(true, PRIVATE_ACCOUNT_TEXT);

        Integer followers = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWERS_NUMBER, FOLLOWER_NUMBER);
        Integer following = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWING_NUMBER);
        Integer posts = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, POSTS_NUMBER, POST_NUMBER);

        potentialFollower.setIsAccountPrivate(isAccountPrivate);
        potentialFollower.setFollowers(followers);
        potentialFollower.setFollowing(following);
        potentialFollower.setPosts(posts);

        System.out.println("PROFILE -" + potentialFollower.getId() + "- WITH FOLLOWERS (" + followers + "), FOLLOWING (" +
                following + "), POSTS (" + posts + "), " + "PRIVATE ACCOUNT (" + isAccountPrivate + ").");

        return followers <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS &&
                followers >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS &&
                following <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS &&
                following >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS &&
                followers - following <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_DIFERENCE_BETWEEN_FOLLOWERS_MINUS_FOLLOWING &&
                posts >= FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS;
    }

    private Boolean isUserNameValid(String userName) {
        return userName != null && userName.length() <= 30;
    }

    private Set<String> processIgList(By locator, ListType listType, Integer listSize) {

        Set<String> resultFromListProcess = new TreeSet<>();

        System.out.println("WE WILL GO TROUGHT LIST ELEMENTS: " + listSize);
        Set<String> alreadyIteratedElements = new TreeSet<>();
        boolean elementsWereProcessedLastLoop = true;

        while (elementsWereProcessedLastLoop) {
            //List is arranged exactly as DOM. NOTE that adding to SET will re-arrange in random order.
            List<WebElement> currentList = WaitDriver.waitAndGetElements(false, locator);

            int sizeBeforeIteration = alreadyIteratedElements.size();
            resultFromListProcess.addAll(iterateListElements(currentList, alreadyIteratedElements, listType, listSize));
            int sizeAfterIteration = alreadyIteratedElements.size();

            elementsWereProcessedLastLoop = sizeAfterIteration > sizeBeforeIteration;
        }
        if (listType == ListType.FOLLOWING_LIST) {
            return resultFromListProcess;
        } else {
            return new TreeSet<>(alreadyIteratedElements);
        }
    }

    private Set<String> iterateListElements(List<WebElement> currentList, Set<String> alreadyIteratedElements, ListType listType, Integer listSize) {

        Set<String> returnedFromList = new TreeSet<>();
        for (WebElement listElement : currentList) {

            String elementId = listElement.getAttribute("title");

            if (alreadyIteratedElements.stream().anyMatch(e -> e.equals(elementId)) || !isUserNameValid(elementId))
                continue;

            //Logic for scrolling, valid for all types of lists
            pageService.moveToElement(WaitDriver.waitAndGetElement(false, WebDriverUtil.CLOSE));
            WaitDriver.sleepForMiliseconds(2000);

            if (alreadyIteratedElements.size() != listSize - 1 && currentList.get(currentList.size() - 1) == listElement) {
                System.out.println("----SCROLLING TO " + elementId + " AND NOT PERFORMING ANY LOGIC");
                ((JavascriptExecutor) AppWebDriver.getWebDriver()).executeScript("arguments[0].scrollIntoView(true);", listElement);
                WaitDriver.sleepForMiliseconds(2000);
            } else {
                System.out.println("----MOVING and PERFORMING LOGIC FOR " + elementId);
                pageService.moveToElement(listElement);
                returnedFromList.addAll(performListLogic(elementId, listType, listElement));
                alreadyIteratedElements.add(elementId);
            }
        }
        return returnedFromList;
    }

    private Set<String> performListLogic(String userName, ListType listType, WebElement webElement) {
        switch (listType) {
            case LIKE_LIST -> {
                return likeListLogic(userName);
            }
            case FOLLOWER_LIST -> {
                return followerListLogic(userName);
            }
            case FOLLOWING_LIST -> {
                return followingListLogic(webElement, userName);
            }
            default -> throw new RuntimeException("Wrong list type");
        }
    }

    private Set<String> likeListLogic(String userName) {
        PotentialFollower potentialFollower = new PotentialFollower();
        potentialFollower.setId(userName);

        if (isFollowerValid(potentialFollower)) {
            potentialFollower.setIsFollowRequested(false);
            potentialFollower.setFollowBackRefused(false);
            potentialFollower.setIsRejectedDueToValidation(false);
            potentialFollowersService.saveOne(potentialFollower);
            System.out.println("USER IS VALID. SAVING IN DB: " + potentialFollower.toString());
        } else {
            System.out.println("USER IS NOT VALID. NOT SAVING IN DB.");
        }
        return Collections.emptySet();
    }

    private Set<String> followerListLogic(String userName) {
        System.out.println("Processing user: " + userName);
        return Collections.emptySet();
    }

    private Set<String> followingListLogic(WebElement listElement, String userName) {
        Set<String> removedUsers = new TreeSet<>();

        if (removeUserQuestionMark(userName)) {
            WebElement button = listElement.findElement(LIST_ELEMENT_BUTTON);
            button.click();
            pageService.waitForButtonAndClickIt(true, UNFOLLOW_CONFIRMATION);

            PotentialFollower potentialFollower = potentialFollowersService.getOptionalById(userName).get();
            potentialFollower.setFollowBackRefused(true);
            potentialFollower.setRemovedFromFollowersAtDate(LocalDate.now());
            potentialFollowersService.saveOne(potentialFollower);
            removedUsers.add(userName);
        }
        return removedUsers;
    }

    public boolean removeUserQuestionMark(String userId) {

        if (potentialFollowersService.isNumberOfRemovalsPerDayReached()) {
            System.out.println("USER " + userId + " WAS *NOT* REMOVED BECAUSE THE NUMBER PER DAY REMOVALS *WAS* REACHED.");
            return false;
        }

        Optional<PotentialFollower> potentialFollowerOptional = potentialFollowersService.getOptionalById(userId);

        //We remove only users from potentialFollowers, not user added by me manually. So if this is empathy we do not remove.
        boolean isUserAPotentialFollower = potentialFollowerOptional.isPresent();
        if (!isUserAPotentialFollower) {
            System.out.println("USER " + userId + " WAS *NOT* REMOVED BECAUSE HE IS *NOT* A POTENTIAL FOLLOWER (WAS ADDED MANUALLY BY ME).");
            return false;
        }

        boolean wasFollowRequestSent = potentialFollowerOptional.get().getFollowRequestSentAtDate() != null;

        if (!wasFollowRequestSent) {
            System.out.println("USER " + userId + " WAS *NOT* REMOVED BECAUSE THE THE FOLLOW REQUEST WAS NOT SENT");
            return false;
        }

        LocalDate followRequestSentAtDate = potentialFollowerOptional.get().getFollowRequestSentAtDate();
        LocalDate currentTimeMinusXDays = LocalDate.now().minusDays(NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER);
        boolean followRequestSentLessThan2DaysAgo = followRequestSentAtDate.isAfter(currentTimeMinusXDays);
        if (followRequestSentLessThan2DaysAgo) {
            System.out.println("USER " + userId + " WAS *NOT* REMOVED BECAUSE THE REQUEST IS RECENT, WE HAVE TO WAIT MORE BEFORE REMOVING. REQUEST DATE: " + followRequestSentAtDate);
            return false;
        }

        if (followerService.isUserInMyFollowerList(userId)) {
            System.out.println("USER " + userId + " WAS NOT REMOVED BECAUSE HE IS IN MY FOLLOWERS LIST.");
            return false;
        }
        //If we reach here it means that the user is:
        //1. A potential follower and we can remove him. If he was not a potential follower we couldn't remove him.
        //2. Has a request sent more than 2 days ago.
        //3. We have more to go until 50 REMOVALS today, and we can go on.
        //4. The user is not in my followers list (does not follow back). So we can safely delete him.
        //5. The user has a Follow request sent (this is obvious if I process the following list, but non the less this is checked too).
        System.out.println("********************************WE CAN SAFELY DELETE THIS USER: " + userId);
        return true;
    }

    private void validatePictureIndex(List<WebElement> pagePictures, Integer pictureIndexFromProfile) {
        if (pagePictures.size() < pictureIndexFromProfile)
            throw new IndexOutOfBoundsException("The profile page does not have that many photos, " + pictureIndexFromProfile + ".");
    }

    private ProcessedPicture getOrSavePictureEntity(WebElement pictureFromProfile, PageAddress pageAddress) {

        String pictureId = pictureFromProfile.getAttribute("href");
        if (StringUtils.isEmpty(pictureId))
            throw new RuntimeException("The selected picture with index 'the index?' is null.");

        Optional<ProcessedPicture> byId = processedPictureJpaRepository.findById(pictureId);

        ProcessedPicture processedPicture;

        if (byId.isEmpty()) {
            processedPicture = new ProcessedPicture(pictureId, false, pageAddress.toString());
            processedPictureJpaRepository.saveAndFlush(processedPicture);
        } else {
            processedPicture = byId.get();
        }
        return processedPicture;
    }

    private boolean isPictureInvalid(ProcessedPicture pictureFromProfile) {

        return pictureFromProfile.getIsProcessed();
    }
}

