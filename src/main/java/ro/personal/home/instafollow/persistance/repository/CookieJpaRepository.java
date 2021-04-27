package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.Cookie;

import java.util.List;

@Repository
public interface CookieJpaRepository extends JpaRepository<Cookie, String>, JpaSpecificationExecutor<Cookie> {

    List<Cookie> getCookiesByUserName(String userName);

    void deleteAllByUserName(String userName);
}
