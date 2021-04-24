package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.Following;

@Repository
public interface FollowingJpaRepository extends JpaRepository<Following, String>, JpaSpecificationExecutor<Following> {
    @Query(value = "UPDATE following set isNoMore= true",nativeQuery = true)
    @Modifying
    public int setAllToIsNoMoreTrue();
}
