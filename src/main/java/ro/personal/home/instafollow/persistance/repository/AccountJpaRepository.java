package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.Account;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {
}
