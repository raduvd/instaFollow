package ro.personal.home.instafollow.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.model.ProcessResult;
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

    Logger logger = LoggerFactory.getLogger(ProcessListService.class);

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

    @Autowired
    private ProcessResultService processResultService;

    /**
     * Ex: If more that 2 days pass and the user does not follow back, we remove him.
     */
    public static Integer NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER = 3;

    private static final By FOLLOWING_NUMBER = By.xpath("//div/span[text()=' following']/span");
    private static final By FOLLOWERS_NUMBER = By.xpath("//div/span[text()=' followers']/span");
    private static final By FOLLOWER_NUMBER = By.xpath("//div/span[text()=' follower']/span");
    private static final By POSTS_NUMBER = By.xpath("//div/span[text()=' posts']/span");
    private static final By POST_NUMBER = By.xpath("//div/span[text()=' post']/span");


    private static final By OTHERS_LIST = By.xpath("//a[text()=' others']");
    private static final By LIKE_LIST = By.xpath("//a[text()=' likes']");
    private static final By OTHERS_LIST_SIZE = By.xpath("//a[text()=' others']/span");
    private static final By LIKE_LIST_SIZE = By.xpath("//a[text()=' likes']/span");

    private static final By LIKES_LIST_USERNAMES = By.xpath("//button/../../div[2]/div/div/span/a");
    /**
     * When we hover over this one, extra info is loaded.
     */
    private static final By LIST_USERNAME_LINKS = By.xpath("//a[@title and @href and @tabindex]");

    private static final By MY_FOLLOWERS_MAIN_BUTTON = By.xpath("//a[@href='/raduvd/followers/']");
    private static final By MY_FOLLOWERS_NUMBER = By.xpath("//a[@href='/raduvd/followers/']/span");
    private static final By MY_FOLLOWING_MAIN_BUTTON = By.xpath("//a[@href='/raduvd/following/']");
    private static final By MY_FOLLOWING_NUMBER = By.xpath("//a[@href='/raduvd/following/']/span");
    //navigate from the link of an element backwards to its button (Follow, Remove or any)
    private static final By LIST_ELEMENT_BUTTON = By.xpath("../../../../../div[2]/button");
    private static final By LIST_ELEMENT_BUTTON_2 = By.xpath("../../../../../..//div[3]/button");

    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS = Integer.MAX_VALUE;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS = Integer.MAX_VALUE;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS = 100;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS = 10;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS = 2;
    private static final Integer MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BELOW_500_FOLLOWERS = 0;
    private static final Integer MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BETWEEN_500_1000_FOLLOWERS = 65;
    private static final Integer MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BETWEEN_1000_1500_FOLLOWERS = 130;
    private static final Integer MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_ABOVE_1500_FOLLOWERS = 200;

    public void tempCheckIsAccountPrivate() {
    }

    /**
     * @param pageAddress             Ex: 'Pe Plaiuri Romanesti' profile page, from where I will get a the last post and check the users that have liked it.
     * @param targetNumberOfFollowers
     */
    public void savePotentialFollowersFrom(PageAddress pageAddress, Integer targetNumberOfFollowers) {

        logger.info("-------------------------GET NEW POTENTIAL FOLLOWERS PROCESS------------------------");

        pageService.changeUser(pageAddress, WebDriverUtil.RADU_VD_1_USERNAME, true);

        //Get the profile pictures
        List<WebElement> pagePictures = WaitDriver.waitAndGetElements(false, WebDriverUtil.PAGE_PICTURES);

        Integer previousListSize = Integer.valueOf(0);
        for (WebElement picture : pagePictures) {

            int allUsersFromDBToFollow = potentialFollowersService.
                    getPotentialFollowersJpaRepository().getAllForFollowing().size();
            logger.info("allUsersFromDBToFollow: {}", allUsersFromDBToFollow);
            logger.info("How many users I need (targetNumberOfFollowers): {}", targetNumberOfFollowers);
            if (allUsersFromDBToFollow >= targetNumberOfFollowers) {
                logger.info("The target is reached so we will not continue to" +
                        " process/save more potential followers.");
                break;
            }

            ProcessedPicture processedPicture = getOrSavePictureEntity(picture, pageAddress);
            if (isPictureInvalid(processedPicture)) {
                logger.info("The Picture was not processed because it was already processed.");
                continue;
            }
            pageService.clickByMovingOnElement(picture);

            //OPEN LIST OF profiles that have LIKEd the picture
            pageService.waitForButtonAndClickIt(false, OTHERS_LIST, LIKE_LIST);
            //process the list and save in DB the usernames
            Integer listSize = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, OTHERS_LIST_SIZE, LIKE_LIST_SIZE);

            if (listSize.equals(previousListSize)) {
                throw new RuntimeException("Two lists with the same size? Something is wrong!"
                        + listSize + " : " + previousListSize);
            }
            previousListSize = listSize;
            ProcessResult processResult = processIgList(LIKES_LIST_USERNAMES, Process.GET_NEW_POTENTIAL_FOLLOWERS, listSize);

            logger.info("WE JUST HOVERED OVER {} USERS (we also saved them in DB). From picture {}",
                    processResult.getFromTotalWeAppliedLogicOn(),
                    processedPicture.getIdPicName());

            processedPicture.setIsProcessed(true);
            processedPictureJpaRepository.saveAndFlush(processedPicture);
            pageService.waitForButtonAndClickIt(false, WebDriverUtil.CLOSE);
        }
        logger.info("I have in DB potential followers valid for following: {}", potentialFollowersService.
                getPotentialFollowersJpaRepository().getAllForFollowing().size());
        pageService.changeUser(pageAddress, WebDriverUtil.RADU_VD_USERNAME, true);
    }

    /**
     * Process the following list and removes users that are not followers.
     * This process should run after refreshFollowers and after removeOrFollow process,
     * because the latter is best way to remove users. This is just as backup. Or run individually.
     *
     * @param numberOfDaysInterval - this is zero indexed!!
     *                             - this process runs once in the number of days indicated by this parameter.
     *                             E.g: If numberOfDaysInterval is 0, the process will run only once that day until midnight.
     *                             If is 7, the process will run once a week.
     */
    public void removeNonFollowers(Integer numberOfDaysInterval) {
        logger.info("-------------------------REMOVE NON FOLLOWERS PROCESS------------------------");

        Process processType = Process.REMOVE_NON_FOLLOWERS;
        List<ProcessResult> processesInNumberOfDaysInterval =
                processResultService.getAllInIntervalOfDays(numberOfDaysInterval, processType);

        if (!processesInNumberOfDaysInterval.isEmpty()) {
            logger.info("THE PROCESS HAS ALREADY RUN IN THE DAYS INTERVAL: " + numberOfDaysInterval);
            return;
        }

        pageService.goToPage(false, PageAddress.INSTAGRAM_MY_ACCOUNT.getLinkToPage());
        pageService.waitForButtonAndClickIt(false, MY_FOLLOWING_MAIN_BUTTON);

        Integer myFollowingNumber = (Integer) pageService.
                getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, MY_FOLLOWING_NUMBER);

        ProcessResult processResult = processIgList(LIST_USERNAME_LINKS, processType, myFollowingNumber);
        processResult.setConfirmedFollowing(myFollowingNumber);
        processResultService.printResultAndValidate(processType, processResult);
        processResultService.getProcessResultJpaRepository().saveAndFlush(processResult);
    }

    /**
     * Gets all followers from list and saves them in DB (isNoMore=false).
     * The old followers that are not found here are set to isNoMore = true, if process is without error.
     */
    public void refreshFollowers(Boolean onlyOnceADay) {
        Process processType = Process.REFRESH_FOLLOWERS;
        List<ProcessResult> refreshFollowersProcessesToday =
                processResultService.getAllInIntervalOfDays(0, processType);

        if (!refreshFollowersProcessesToday.isEmpty() && onlyOnceADay) {
            logger.info("THE REFRESH FOLLOWING PROCESS WAS ALREADY DONE TODAY");
            return;
        }

        logger.info("-------------------------REFRESHING FOLLOWERS------------------------");

        pageService.goToPage(false, PageAddress.INSTAGRAM_MY_ACCOUNT.getLinkToPage());

        Integer myFollowersNumber = (Integer) pageService.
                getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, MY_FOLLOWERS_NUMBER);

        pageService.waitForButtonAndClickIt(false, MY_FOLLOWERS_MAIN_BUTTON);

        ProcessResult processResult = processIgList(LIST_USERNAME_LINKS, Process.REFRESH_FOLLOWERS, myFollowersNumber);
        Set<String> followersFoundInWebDriver = processResult.getProcessedUserList();
        //TODO this logic will cause the follower table not to be consistent
        //TODO debug why sometimes the list is not process corectlu- i have noticed that it failed wehn 160 list size, and was ok at 166
        //TODO add the saving in db logic in the positive if
        //TODO add an extra check to really be sure that the follower number is correct
        if (myFollowersNumber - followersFoundInWebDriver.size() > 5) {
            throw new RuntimeException("The followers Found In Web Driver(" + followersFoundInWebDriver.size() +
                    ") are not equal with the followers number from my page(" + myFollowersNumber +
                    "). The difference is bigger than 5 so something is really wrong.");
        } else {
            followingService.setIsNoMore();
            List<Followers> followers = followerService.createFollowers(new ArrayList<>(followersFoundInWebDriver));
            processResultService.getProcessResultJpaRepository().saveAndFlush(processResult);
            logger.info("--------------------------WE ADDED IN DB FOLLOWERS: " + followers.size());
            logger.info("My Followers: " + followers);
            logger.info("The followers Found In Web Driver(" + followersFoundInWebDriver.size() + ") and followers number from my page("
                    + myFollowersNumber + ").");

            potentialFollowersService.updateFollowRequestSentConfirmed(followersFoundInWebDriver);

        }
    }

    public boolean isFollowerValid(PotentialFollower potentialFollower) {

        boolean isUserAFollower = followingService.getFollowingJpaRepository().findById(potentialFollower.getId()).isPresent();
        boolean isUserAPotentialFollower = potentialFollowersService.getOptionalById(potentialFollower.getId()).isPresent();
        if (isUserAFollower || isUserAPotentialFollower) return false;

        boolean isAccountPrivate = null != WaitDriver.waitAndGetElement(true, WebDriverUtil.THE_ACCOUNT_IS_PRIVATE);

        Integer followers = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWERS_NUMBER, FOLLOWER_NUMBER);
        Integer following = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWING_NUMBER);
        Integer posts = (Integer) pageService.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, POSTS_NUMBER, POST_NUMBER);

        potentialFollower.setIsAccountPrivate(isAccountPrivate);
        potentialFollower.setFollowers(followers);
        potentialFollower.setFollowing(following);
        potentialFollower.setPosts(posts);

        logger.debug("PROFILE - {} - WITH FOLLOWERS ({}), FOLLOWING ({}), POSTS ({}), PRIVATE ACCOUNT ({}).",
                potentialFollower.getId(),
                followers,
                following,
                posts,
                isAccountPrivate);

        return validateUserInfo(followers, following, posts);
    }

    public boolean validateUserInfo(Integer followers, Integer following, Integer posts) {

        if (followers < 500 && followers - following > MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BELOW_500_FOLLOWERS) {
            return false;
        }

        if (followers > 500 && followers < 1000 && followers - following > MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BETWEEN_500_1000_FOLLOWERS) {
            return false;
        }

        if (followers > 1000 && followers < 1500 && followers - following > MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_BETWEEN_1000_1500_FOLLOWERS) {
            return false;
        }

        if (followers > 1500 && followers - following > MAX_DIF_BETWEEN_FOLLOWERS_MINUS_FOLLOWING_ACCOUNTS_WITH_ABOVE_1500_FOLLOWERS)
            return false;

        return followers <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS &&
                followers >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS &&
                following <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS &&
                following >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS &&
                posts >= FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS;
    }

    private Boolean isUserNameValid(String userName) {
        return userName != null && userName.length() <= 30;
    }

    private ProcessResult processIgList(By locator, Process process, Integer listSize) {
        ProcessResult processResult = new ProcessResult(process);

        Set<String> resultFromListProcess = new TreeSet<>();

        logger.info("WE WILL GO TROUGHT LIST ELEMENTS: " + listSize);
        Set<String> alreadyIteratedElements = new TreeSet<>();
        boolean elementsWereProcessedLastLoop = true;

        while (elementsWereProcessedLastLoop) {
            //List is arranged exactly as DOM. NOTE that adding to SET will re-arrange in random order.
            List<WebElement> currentList = WaitDriver.waitAndGetElements(false, locator);

            int sizeBeforeIteration = alreadyIteratedElements.size();
            resultFromListProcess.addAll(iterateListElements(currentList, alreadyIteratedElements, process, listSize));
            int sizeAfterIteration = alreadyIteratedElements.size();

            elementsWereProcessedLastLoop = sizeAfterIteration > sizeBeforeIteration;
        }

        processResult.setTotalProcessedUsers(alreadyIteratedElements.size());
        processResult.setFromTotalWeAppliedLogicOn(resultFromListProcess.size());
        processResult.setProcessedUserList(alreadyIteratedElements);

        return processResult;
    }

    private Set<String> iterateListElements
            (List<WebElement> currentList, Set<String> alreadyIteratedElements, Process process, Integer listSize) {

        Set<String> returnedFromList = new TreeSet<>();
        for (WebElement listElement : currentList) {

            String elementId = listElement.getAttribute("title");

            if (alreadyIteratedElements.stream().anyMatch(e -> e.equals(elementId)) || !isUserNameValid(elementId))
                continue;

            //Logic for scrolling, valid for all types of lists
            pageService.moveToElement(WaitDriver.waitAndGetElement(false, WebDriverUtil.CLOSE));
            WaitDriver.sleepForMiliseconds(2000);

            if (alreadyIteratedElements.size() != listSize - 1 &&
                    currentList.get(currentList.size() - 1) == listElement) {
                logger.debug("----SCROLLING TO {} AND NOT PERFORMING ANY LOGIC", elementId);
                ((JavascriptExecutor) AppWebDriver.getWebDriver()).executeScript("arguments[0].scrollIntoView(true);", listElement);
                WaitDriver.sleepForMiliseconds(2000);
            } else {
                logger.debug("----MOVING and PERFORMING LOGIC FOR " + elementId);
                returnedFromList.add(performListLogic(elementId, process, listElement));
                alreadyIteratedElements.add(elementId);
            }
        }
        return returnedFromList;
    }

    private String performListLogic(String userName, Process process, WebElement webElement) {
        switch (process) {
            case GET_NEW_POTENTIAL_FOLLOWERS -> {
                return likeListLogic(webElement, userName);
            }
            case REFRESH_FOLLOWERS -> {
                return followerListLogic(webElement, userName);
            }
            case REMOVE_NON_FOLLOWERS -> {
                return followingListLogic(webElement, userName);
            }
            default -> throw new RuntimeException("Wrong process type for this logic.");
        }
    }

    private String likeListLogic(WebElement listElement, String userName) {

        if (potentialFollowersService.getOptionalById(userName).isPresent()) {
            logger.debug("User is already in the DB we will not hover.");
        } else {
            logger.info("Hovering...");
            pageService.moveToElement(listElement);

            PotentialFollower potentialFollower = new PotentialFollower();
            potentialFollower.setId(userName);

            if (isFollowerValid(potentialFollower)) {
                potentialFollower.setIsRejectedDueToValidation(false);
                logger.debug("USER IS VALID. WILL SAVE IN DB: " + potentialFollower.toString());
            } else {
                potentialFollower.setIsRejectedDueToValidation(true);
                logger.debug("USER IS NOT VALID. BUT STILL SAVING IN DB; " + potentialFollower.toString());
            }

            potentialFollower.setPageCanBeOpened(true);
            potentialFollower.setRemovalConfirmed(false);
            potentialFollower.setFollowRequestSentConfirmed(false);
            potentialFollower.setIsFollowRequested(false);
            potentialFollower.setFollowBackRefused(false);
            potentialFollower.setAddedAt(LocalDate.now());
            potentialFollowersService.saveOne(potentialFollower);
        }
        return userName;
    }

    private String followerListLogic(WebElement listElement, String userName) {
        logger.debug("Processing user: " + userName);
        return StringUtils.EMPTY;
    }

    private String followingListLogic(WebElement listElement, String userName) {

        if (removeUserQuestionMark(userName)) {

            WebElement button = pageService.findSubElementBy(false, listElement, LIST_ELEMENT_BUTTON, LIST_ELEMENT_BUTTON_2);
            button.click();
            pageService.waitForButtonAndClickIt(true, WebDriverUtil.UNFOLLOW_CONFIRMATION);

            PotentialFollower potentialFollower = potentialFollowersService.getOptionalById(userName).get();
            potentialFollower.setFollowBackRefused(true);
            potentialFollower.setRemovedFromFollowersAtDate(LocalDate.now());
            potentialFollower.setRemovalConfirmed(false);
            potentialFollower.setFollowRequestSentConfirmed(true);
            potentialFollowersService.saveOne(potentialFollower);
            WaitDriver.sleepForMiliseconds(30000);
            return userName;
        }
        return StringUtils.EMPTY;
    }

    public boolean removeUserQuestionMark(String userId) {

        if (potentialFollowersService.isNumberOfRemovalsPerDayReached()) {
            logger.debug("USER {} WAS *NOT* REMOVED BECAUSE THE NUMBER PER DAY REMOVALS *WAS* REACHED.", userId);
            return false;
        }

        Optional<PotentialFollower> potentialFollowerOptional = potentialFollowersService.getOptionalById(userId);

        //We remove only users from potentialFollowers, not user added by me manually. So if this is empathy we do not remove.
        boolean isUserAPotentialFollower = potentialFollowerOptional.isPresent();
        if (!isUserAPotentialFollower) {
            logger.debug("USER {} WAS *NOT* REMOVED BECAUSE HE IS *NOT* A POTENTIAL FOLLOWER (WAS ADDED MANUALLY BY ME).", userId);
            return false;
        }

        boolean wasFollowRequestSent = potentialFollowerOptional.get().getFollowRequestSentAtDate() != null;

        if (!wasFollowRequestSent) {
            logger.debug("USER {} WAS *NOT* REMOVED BECAUSE THE THE FOLLOW REQUEST WAS NOT SENT", userId);
            return false;
        }

        LocalDate followRequestSentAtDate = potentialFollowerOptional.get().getFollowRequestSentAtDate();
        LocalDate currentTimeMinusXDays = LocalDate.now().minusDays(NR_OF_DAYS_BEFORE_REMOVING_FOLLOWER);
        boolean followRequestSentLessThan2DaysAgo = followRequestSentAtDate.isAfter(currentTimeMinusXDays);
        if (followRequestSentLessThan2DaysAgo) {
            logger.debug("USER {} WAS *NOT* REMOVED BECAUSE THE REQUEST IS RECENT," +
                    " WE HAVE TO WAIT MORE BEFORE REMOVING. REQUEST DATE: {}", userId, followRequestSentAtDate);
            return false;
        }

        if (followerService.isUserInMyFollowerList(userId)) {
            logger.debug("USER {} WAS NOT REMOVED BECAUSE HE IS IN MY FOLLOWERS LIST.", userId);
            return false;
        }
        //If we reach here it means that the user is:
        //1. A potential follower and we can remove him. If he was not a potential follower we couldn't remove him.
        //2. Has a request sent more than 2 days ago.
        //3. We have more to go until 50 REMOVALS today, and we can go on.
        //4. The user is not in my followers list (does not follow back). So we can safely delete him.
        //5. The user has a Follow request sent (this is obvious if I process the following list, but non the less this is checked too).
        logger.debug("********************************WE CAN SAFELY DELETE THIS USER: " + userId);
        return true;
    }

    private ProcessedPicture getOrSavePictureEntity(WebElement pictureFromProfile, PageAddress pageAddress) {

        String pictureId = pictureFromProfile.getAttribute("href");

        logger.info("Processing... picture: " + pictureId);

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

