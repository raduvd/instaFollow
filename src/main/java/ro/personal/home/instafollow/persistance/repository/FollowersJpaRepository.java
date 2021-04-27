package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.Followers;

import java.util.List;

@Repository
public interface FollowersJpaRepository extends JpaRepository<Followers, String>, JpaSpecificationExecutor<Followers> {

    @Query(value = "UPDATE followers set isNoMore= true", nativeQuery = true)
    @Modifying
    int setAllToIsNoMoreTrue();

    List<Followers> getAllByIsNoMore(Boolean isNoMore);
}
