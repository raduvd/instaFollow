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

    @Query(value = "SELECT * FROM potentialfollowers pf JOIN followers f on f.id = pf.id " +
            "where " +
            "pf.isfollowrequested = true " +
            "and pf.followRequestSentAtDate < NOW() - INTERVAL '2 DAY' ", nativeQuery = true)
    List<PotentialFollower> getRequestedUsersWhoFollowedBack();

    @Query(value = "SELECT * FROM potentialfollowers pf " +
            "where " +
            "pf.isfollowrequested = true " +
            "and pf.followRequestSentAtDate < NOW() - INTERVAL '2 DAY'", nativeQuery = true)
    List<PotentialFollower> getAllRequestedUsers();
}
