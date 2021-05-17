package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;

import java.util.List;
import java.util.Set;

@Repository
public interface PotentialFollowersJpaRepository extends JpaRepository<PotentialFollower, String>, JpaSpecificationExecutor<PotentialFollower> {

    List<PotentialFollower> getAllByIsAccountPrivate(Boolean isAccountPrivate);
    //TODO a lot of these methods are similar, so parametrize them and make only one?
    // or make with specification!!

    /**
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
            "AND removedFromFollowersAtDate < NOW() " +
            "AND pageCanBeOpened = true " +
            "AND isRejectedDueToValidation = false " +
            "ORDER BY removedFromFollowersAtDate", nativeQuery = true)
    List<PotentialFollower> getAllForConfirmingRemoval();

    /**
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that I have followed them more than 3 days ago - have a followRequestSentAtDate newer than 2 days ago.
     * that have an openable page
     * that have removedFromFollowersAtDate is NULL - did not remove them yet
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND removedFromFollowersAtDate IS null " +
            "AND followRequestSentAtDate < NOW() - INTERVAL '3 DAY' " +
            "AND pageCanBeOpened = true " +
            "AND isRejectedDueToValidation = false " +
            "ORDER BY followRequestSentAtDate", nativeQuery = true)
    List<PotentialFollower> getAllForRemoval();

    //

    /**
     * @return a list with all {@link PotentialFollower}
     * that are not in the Followers table,
     * that have followRequestSentAtDate is NULL - did not follow them yet
     * that have an openable page
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id NOT IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND followRequestSentAtDate IS null " +
            "AND isRejectedDueToValidation = false " +
            "AND pageCanBeOpened = true", nativeQuery = true)
    List<PotentialFollower> getAllForFollowing();


    /**
     * @return a list with all {@link PotentialFollower}
     * that have a followRequestSentAtDate - I have sent one
     * that have followRequestSentConfirmed = true
     * that have more than 3 days since followRequestSentAtDate
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE followRequestSentAtDate IS NOT null " +
            "AND followRequestSentConfirmed = true " +
            "AND followRequestSentAtDate < NOW() - INTERVAL '3 DAY' ", nativeQuery = true)
    List<PotentialFollower> getAllRequestedUsers();

    /**
     * @return a list with all {@link PotentialFollower}
     * that are in the Followers table,
     * that have a followRequestSentAtDate - I have sent a request
     * that have followRequestSentConfirmed = true or false because for followers I do not confirm sending
     * that have more than 3 days since followRequestSentAtDate
     */
    @Query(value = "SELECT * FROM public.potentialfollowers " +
            "WHERE id IN (SELECT pf.id FROM potentialfollowers pf JOIN followers f ON pf.id = f.id WHERE f.isNoMore = false) " +
            "AND followRequestSentAtDate IS NOT null " +
            "AND followRequestSentConfirmed = true " +
            "AND followRequestSentAtDate < NOW() - INTERVAL '3 DAY' ", nativeQuery = true)
    List<PotentialFollower> getAllThatFollowedBack();

    @Query(value = "UPDATE potentialFollowers set followRequestSentConfirmed = true WHERE id IN :ids", nativeQuery = true)
    @Modifying
    void updateFollowRequestSentConfirmed(@Param("ids") Set<String> ids);

}
