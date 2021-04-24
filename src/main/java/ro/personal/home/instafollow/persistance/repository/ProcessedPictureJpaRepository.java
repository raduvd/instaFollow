package ro.personal.home.instafollow.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ro.personal.home.instafollow.persistance.model.ProcessedPicture;

@Repository
public interface ProcessedPictureJpaRepository extends JpaRepository<ProcessedPicture, String>, JpaSpecificationExecutor<ProcessedPicture> {
}
