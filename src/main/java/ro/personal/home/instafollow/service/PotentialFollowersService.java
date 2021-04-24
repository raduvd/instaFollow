package ro.personal.home.instafollow.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.persistance.model.PotentialFollower;
import ro.personal.home.instafollow.persistance.repository.PotentialFollowersJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Data
public class PotentialFollowersService {

    @Autowired
    private PotentialFollowersJpaRepository potentialFollowersJpaRepository;

    public PotentialFollower saveOne(PotentialFollower potentialFollower) {

        return potentialFollowersJpaRepository.saveAndFlush(potentialFollower);
    }

    public List<PotentialFollower> saveAll(List<PotentialFollower> potentialFollowers) {
        List<PotentialFollower> returnedList = potentialFollowersJpaRepository.saveAll(potentialFollowers);
        potentialFollowersJpaRepository.flush();
        return returnedList;
    }

    public List<PotentialFollower> getPotentialFollowers(Specification<PotentialFollower> spec) {
        return potentialFollowersJpaRepository.findAll(spec);
    }

    private Specification<PotentialFollower> getPotentialFollowerWithIsFollowRequested(Boolean isFollowRequested) {
        return (root, query, cb) -> cb.equal(root.get("isFollowRequested"), isFollowRequested);
    }

    private Specification<PotentialFollower> getPotentialFollowerWithIsRejectedDueToValidation(Boolean isRejectedDueToValidation) {
        return (root, query, cb) -> cb.equal(root.get("isRejectedDueToValidation"), isRejectedDueToValidation);
    }

    public List<PotentialFollower> getPotentialFollowerForFollowing() {
        return getPotentialFollowers(getPotentialFollowerWithIsFollowRequested(false).and(getPotentialFollowerWithIsRejectedDueToValidation(false)));
    }

    private Specification<PotentialFollower> getPotentialFollowerForRemovalSpecification() {
        return (root, query, cb) -> cb.equal(root.get(""), "today");
    }

    public List<PotentialFollower> createPotentialFollowers(List<String> followersIds) {
        return saveAll(
                followersIds.stream().map(
                        f -> new PotentialFollower(
                                f,
                                false,
                                false,
                                null,
                                false,
                                false,
                                null,
                                null,
                                null)).
                        collect(Collectors.toList()));
    }
}
