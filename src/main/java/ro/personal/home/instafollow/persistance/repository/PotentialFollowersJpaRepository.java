package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;

@Repository
public interface PotentialFollowersJpaRepository extends JpaRepository<PotentialFollower, String>, JpaSpecificationExecutor<PotentialFollower> {
}
