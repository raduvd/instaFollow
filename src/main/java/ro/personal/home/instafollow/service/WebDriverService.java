package ro.personal.home.instafollow.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ro.personal.home.instafollow.enums.ElementValue;
import ro.personal.home.instafollow.enums.ErrorType;
import ro.personal.home.instafollow.enums.PageAddress;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.model.ProcessedPicture;
import ro.personal.home.instafollow.persistance.repository.ProcessedPictureJpaRepository;
import ro.personal.home.instafollow.service.functionalinterface.ProfileSave;
import ro.personal.home.instafollow.webDriver.model.Errors;
import ro.personal.home.instafollow.webDriver.model.Page;
import ro.personal.home.instafollow.webDriver.model.Result;
import ro.personal.home.instafollow.webDriver.webDriver.WaitDriver;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Data
public class WebDriverService {

    @Autowired
    private ProcessedPictureJpaRepository processedPictureJpaRepository;

    @Autowired
    private FollowerService followerService;

    @Autowired
    private FollowingService followingService;

    @Autowired
    private PotentialFollowersService potentialFollowersService;

    private static final Integer FOLLOW_REQUESTS_PER_DAY = 50;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS = 2500;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS = 2500;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS = 100;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS = 100;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS = 10;
    private static final Integer FOLLOW_ACCOUNTS_WITH_MAXIMUM_DIFERENCE_BETWEEN_FOLLOWERS_MINUS_FOLLOWING = 200;

    /*Finds all element in a page that have an attribute alt (description of photo) and srcset (poza in sine), these are the elements with photos from a profile*/
    public static final By PAGE_PICTURES = By.xpath("//*[@alt and @srcset]/../../..");
    public static final By OTHERS_LIST = By.xpath("//a[text()=' others']");
    //This will go back from an element from USERNAMES_FROM_LIST to its correspon
    public static final By LIKES_LIST_USERNAMES = By.xpath("//button[text()='Like']/../../div[2]/div/div/span/a");
    public static final By FOLLOWING_LIST_USERNAMES = By.xpath("//a[@title and @href and @tabindex]");
    public static final By FOLLOWERS_LIST_USERNAMES = By.xpath("//a[@title and @href and @tabindex]");
    public static final By REMOVE_BUTTONS = By.xpath("//button[text()='Remove']");
    public static final By LIKE = By.xpath("//*[@aria-label='Like' and @height=24]");
    public static final By UNLIKE = By.xpath("//*[@aria-label='Unlike']");
    public static final By CLOSE = By.xpath("//*[@aria-label='Close']");
    public static final By FOLLOW = By.xpath("//button[text()='Follow']");
    public static final By FOLLOWING_NUMBER = By.xpath("//*[text()=' following']/span");
    public static final By FOLLOWERS_NUMBER = By.xpath("//*[text()=' followers']/span");
    public static final By FOLLOWER_NUMBER = By.xpath("//*[text()=' follower']/span");
    public static final By POSTS_NUMBER = By.xpath("//*[text()=' posts']/span");
    public static final By POST_NUMBER = By.xpath("//*[text()=' post']/span");
    public static final By THIS_ACCOUNT_IS_PRIVATE = By.xpath("//*[text()='This Account is Private']");

    public void removeFollowersThatDoNotFollowBack() {
        //TODO arrange the list according to date and remove first the oldest request
    }

    public static final By USERNAMES_FROM_MY_FOLLOWER_LIST = By.xpath("//button[text()='Remove']/../../div[2]/div/div/div/span/a");
    public static final By MY_FOLLOWERS = By.xpath("//a[@href='/raduvd/followers/']");
    public static final By MY_FOLLOWING = By.xpath("//a[@href='/raduvd/following/']");

    public void saveFollowersAndFollowing(Page page) {
        page.goToPage(PageAddress.INSTAGRAM_MY_ACCOUNT.getLinkToPage());

        //mark all at no more
        followerService.setIsNoMore();
        followingService.setIsNoMore();

        //populate again with noMore false ==> the isNoMore are no more
        saveList(page, MY_FOLLOWERS, FOLLOWERS_LIST_USERNAMES, p -> followerService.createFollowers(p));
        page.waitForButtonAndClickIt(CLOSE);
        saveList(page, MY_FOLLOWING, FOLLOWING_LIST_USERNAMES, p -> followingService.createFollowers(p));
    }

    public void saveList(Page page, By listLocator, By listElementLocator, ProfileSave profileSave) {
        page.waitForButtonAndClickIt(listLocator);
        processList(page, listElementLocator, profileSave);
    }

    public void followPotentialFollowers(Page page, Integer numerOfPotentialFollowersToFollow) {

        List<PotentialFollower> profilesToFollow = potentialFollowersService.getPotentialFollowerForFollowing();

        System.out.println("POTENTIAL FOLLOWERS TO PROCESS: " + profilesToFollow.size());

        for (PotentialFollower potentialFollower : profilesToFollow) {

            if (isNumberOfFollowsPerDayReached(page) || numerOfPotentialFollowersToFollow-- <= 0) break;
            //GO to profile page
            page.goToPage(createPageAddress(potentialFollower.getId()));

            if (isFollowerValid(page, potentialFollower)) {

                if (!isAccountPrivate()) {
                    //click on first picture and like it
                    List<WebElement> pagePictures = WaitDriver.waitAndGetElements(true, PAGE_PICTURES);
                    page.clickByMovingOnElement(pagePictures.get(0));
                    page.waitForButtonAndClickIt(LIKE, UNLIKE);
                    page.waitForButtonAndClickIt(CLOSE);
                }
                //Follow
                page.waitForButtonAndClickIt(FOLLOW);
                potentialFollower.setIsFollowRequested(true);
                potentialFollower.setFollowRequestSentAtDate(LocalDate.now());
            } else {

                potentialFollower.setIsRejectedDueToValidation(true);
            }

            potentialFollowersService.saveOne(potentialFollower);
        }

        printResultInConsoleLog(page.getResult());
    }

    private boolean isAccountPrivate() {

        return WaitDriver.waitAndGetElement(true, THIS_ACCOUNT_IS_PRIVATE) != null;
    }

    public boolean isFollowerValid(Page page, PotentialFollower potentialFollower) {

        Integer followers = (Integer) page.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWERS_NUMBER, FOLLOWER_NUMBER);
        Integer following = (Integer) page.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, FOLLOWING_NUMBER);
        Integer posts = (Integer) page.getValueFromElement(false, ElementValue.NUMBER_WITH_K_COMA_OR_POINT, POSTS_NUMBER, POST_NUMBER);

        potentialFollower.setFollowers(followers);
        potentialFollower.setFollowing(following);
        potentialFollower.setPosts(posts);

        System.out.println("PROFILE WITH FOLLOWERS (" + followers + "), FOLLOWING (" + following + "), POSTS (" + posts + ").");

        return followers <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWERS &&
                followers >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWERS &&
                following <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_FOLLOWINGS &&
                following >= FOLLOW_ACCOUNTS_WITH_MINIMUM_FOLLOWINGS &&
                followers - following <= FOLLOW_ACCOUNTS_WITH_MAXIMUM_DIFERENCE_BETWEEN_FOLLOWERS_MINUS_FOLLOWING &&
                posts >= FOLLOW_ACCOUNTS_WITH_MINIMUM_POSTS;
    }

    private boolean isNumberOfFollowsPerDayReached(Page page) {
        List<PotentialFollower> followRequestSentAtDate =
                potentialFollowersService.getPotentialFollowers(
                        (r, q, c) -> c.equal(r.get("followRequestSentAtDate"), LocalDate.now()));

        if (followRequestSentAtDate.size() >= FOLLOW_REQUESTS_PER_DAY) {
            page.getResult().getMessages().add("TODAY I ALREADY REACHED MAXIMUM NUMBER OF FOLLOWED ACCOUNTS ");
            return true;
        }
        return false;
    }

    private String createPageAddress(String relativeUrl) {
        return PageAddress.INSTAGRAM_RAW.getLinkToPage() + relativeUrl;
    }

    /**
     * @param pageAddresses           Ex: 'Pe Plaiuri Romanesti' profile page, from where I will a post check the users that have liked it.
     * @param pictureIndexFromProfile the profile has a multitude of picture, this index will chose one of these pictures. Usually this is zero always.
     */
    public void savePotentialFollowersFrom(List<PageAddress> pageAddresses, Integer pictureIndexFromProfile, Page page) {

        for (PageAddress pageAddress : pageAddresses) {
            //Go to page/profile
            page.goToPage(pageAddress.getLinkToPage());

            //Get the profile pictures
            List<WebElement> pagePictures = WaitDriver.waitAndGetElements(false, PAGE_PICTURES);

            //Get the picture according to pictureIndexFromProfile
            validatePictureIndex(pagePictures, pictureIndexFromProfile);
            WebElement pictureFromProfile = pagePictures.get(pictureIndexFromProfile);
            ProcessedPicture processedPicture = getOrSavePictureEntity(pictureFromProfile, pageAddress);
            if (isPictureInvalid(processedPicture)) {
                page.getResult().getMessages().add(
                        "Picture index " + pictureIndexFromProfile + " from profile " +
                                pageAddress + "was not processed because it is already processed");
                continue;
            }
            page.clickByMovingOnElement(pictureFromProfile);

            //OPEN LIST OF profiles that have LIKEd the picture
            page.waitForButtonAndClickIt(OTHERS_LIST);
            //process the list and save in DB the usernames
            processList(page, LIKES_LIST_USERNAMES, p -> potentialFollowersService.createPotentialFollowers(p));

            processedPicture.setIsProcessed(true);
            processedPictureJpaRepository.saveAndFlush(processedPicture);

            printResultInConsoleLog(page.getResult());
        }
    }

    private void processList(Page page, By locator, ProfileSave profileSave) {
        boolean process = true;
        Set<String> userFromListSet = new TreeSet<>();

        while (process) {
            List<WebElement> webElements = WaitDriver.waitAndGetElements(false, locator);
            List<String> userFromListList = webElements.stream().map(f -> f.getAttribute("title")).collect(Collectors.toList());

            Integer userFromListSetSize = userFromListSet.size();
            userFromListSet.addAll(userFromListList);
            Integer userFromListSetSizeAfterAddition = userFromListSet.size();

            process = !userFromListSetSize.equals(userFromListSetSizeAfterAddition);

            profileSave.save(new ArrayList<>(userFromListSet));

            page.moveCursorToElementAndScroll(webElements.size() > 0 ? webElements.get(webElements.size() - 1) : null);
        }
    }

    private void processNonChangingList(Page page, By locator, ProfileSave profileSave) {
        List<WebElement> elList = WaitDriver.waitAndGetElements(false, locator);

        Actions actions = new Actions(page.getWebDriver());
        actions.moveToElement(elList.size() > 0 ? elList.get(elList.size() - 1) : null).click().perform();

        long t = System.currentTimeMillis();
        long end = t + 20000;
        while (System.currentTimeMillis() < end) {
            actions.sendKeys(Keys.DOWN).perform();
        }

        List<WebElement> webElementList = WaitDriver.waitAndGetElements(false, locator);
        Set<String> userFromListSet = webElementList.stream().map(f -> f.getAttribute("title")).collect(Collectors.toSet());
        profileSave.save(new ArrayList<>(userFromListSet));
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

    private void printResultInConsoleLog(Result result) {
        System.out.println("------------------------------------ ..\nSUM-UP\n..\n");
        result.getMessages().forEach(System.out::println);
        System.out.println("------------------------------------TOTAL number of errors: " + result.getAllErrorsList().size());
        System.out.println("------------------------------------" + ErrorType.INVALID_VALUE.name() + " - number of errors: " + result.getInvalidValueList().size() + " ---- description: " + ErrorType.INVALID_VALUE.description);
        System.out.println("------------------------------------" + ErrorType.WAITING_TIMEOUT.name() + " - number of errors: " + result.getWaitingTimeoutList().size() + " ---- description: " + ErrorType.WAITING_TIMEOUT.description);
        System.out.println("------------------------------------" + ErrorType.CASTING_EXCEPTION.name() + " - number of errors: " + result.getCastingExceptionList().size() + " ---- description: " + ErrorType.CASTING_EXCEPTION.description);
        System.out.println("------------------------------------" + ErrorType.ELEMENT_NOT_FOUND.name() + " - number of errors: " + result.getElementNotFoundList().size() + " ---- description: " + ErrorType.ELEMENT_NOT_FOUND.description);
        System.out.println("------------------------------------" + ErrorType.ERROR_IN_JAVA_LOGIC.name() + " - number of errors: " + result.getErrorInJavaLogicList().size() + " ---- description: " + ErrorType.ERROR_IN_JAVA_LOGIC.description);

        final List<Errors> sortedAllErrorList = result.getAllErrorsList().stream().
                sorted(Comparator.comparing(Errors::getValue)).collect(Collectors.toList());

        System.out.println("..\nSHORT ERRORS\n..\n");
        sortedAllErrorList.forEach(e -> System.out.println(e.getValue() + " - " + e.getErrorType() + " - " + e.getErrorMessage() + " - " + e.getPageAddress().name()));

        Set<Errors> uniqueValueErrors = new TreeSet<>(Comparator.comparing(Errors::getValue));
        uniqueValueErrors.addAll(sortedAllErrorList);

        System.out.println("..\nSTACK TRACES\n..\n");
        uniqueValueErrors.forEach(e -> {
            if (e.getException() != null) {
                System.out.println(e.getValue() + " - " + e.getErrorType() + " - " + e.getErrorMessage() + " - " + e.getPageAddress().name());
                e.getException().printStackTrace();
            }
        });
    }

    private void validatePictureIndex(List<WebElement> pagePictures, Integer pictureIndexFromProfile) {
        if (pagePictures.size() < pictureIndexFromProfile)
            throw new IndexOutOfBoundsException("The profile page does not have that many photos, " + pictureIndexFromProfile + ".");
    }

    private boolean isPictureInvalid(ProcessedPicture pictureFromProfile) {

        return pictureFromProfile.getIsProcessed();
    }
}
