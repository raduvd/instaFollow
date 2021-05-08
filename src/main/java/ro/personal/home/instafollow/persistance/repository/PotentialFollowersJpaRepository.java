package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;

import java.util.List;

@Repository
public interface PotentialFollowersJpaRepository extends JpaRepository<PotentialFollower, String>, JpaSpecificationExecutor<PotentialFollower> {

    List<PotentialFollower> getAllByIsAccountPrivate(Boolean isAccountPrivate);
//TODO a lot of these methods are similar, so parametrize them and make only one?
// or make with specification!!

    /**
     *
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that are not confirmed from removal OR following
     * that have an openable page
     * and that I have removed them once, but not today - have a removedFromFollowersAtDate other than today
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND (removalconfirmed IS null OR removalconfirmed = false) " +
            "AND removedFromFollowersAtDate IS NOT null " +
            "AND removedFromFollowersAtDate = NOW() " +
            "AND pageCanBeOpened = true " +
            "ORDER BY removedFromFollowersAtDate", nativeQuery = true)
    List<PotentialFollower> getAllForConfirmingRemoval();

    /**
     *
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that are not confirmed from following
     * that have an openable page
     * and that I have followed them less than 2 days ago - have a followRequestSentAtDate newer than 2 days ago.
     * and also removedFromFollowersAtDate is NULL
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND (followrequestsentconfirmed IS null OR followrequestsentconfirmed = false) " +
            "AND removedFromFollowersAtDate IS null " +
            "AND followRequestSentAtDate > NOW() - INTERVAL '2 DAY' " +
            "AND pageCanBeOpened = true" +
            "ORDER BY followRequestSentAtDate", nativeQuery = true)
    List<PotentialFollower> getAllForConfirmingFollowing();

    /**
     *
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that are not confirmed from following
     * that I have followed them more than 2 days ago - have a followRequestSentAtDate newer than 2 days ago.
     * that have an openable page
     * that have removedFromFollowersAtDate is NULL - did not remove them yet
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND (followrequestsentconfirmed IS null OR followrequestsentconfirmed = false) " +
            "AND removedFromFollowersAtDate IS null " +
            "AND followRequestSentAtDate < NOW() - INTERVAL '2 DAY' " +
            "AND pageCanBeOpened = true" +
            "ORDER BY followRequestSentAtDate", nativeQuery = true)
    List<PotentialFollower> getAllForRemoval();

    /**
     *
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that have followRequestSentAtDate is NULL - did not follow them yet
     * that have an openable page
     * that have removedFromFollowersAtDate is NULL - did not remove them yet
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND removedFromFollowersAtDate IS null " +
            "AND followRequestSentAtDate IS null " +
            "AND isRejectedDueToValidation = false " +
            "AND pageCanBeOpened = true", nativeQuery = true)
    List<PotentialFollower> getAllForFollowing();

    @Query(value = "SELECT * FROM potentialfollowers pf " +
            "where " +
            "pf.isfollowrequested = true " +
            "and pf.followRequestSentAtDate < NOW() - INTERVAL '2 DAY'", nativeQuery = true)
    List<PotentialFollower> getAllRequestedUsers();
}
