package ro.personal.home.instafollow.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.personal.home.instafollow.persistance.model.Following;
import ro.personal.home.instafollow.persistance.repository.FollowingJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Data
public class FollowingService {

    @Autowired
    private FollowingJpaRepository followingJpaRepository;

    public List<Following> saveAll(List<Following> followers) {
        List<Following> followers1 = followingJpaRepository.saveAll(followers);
        followingJpaRepository.flush();

        return followers1;
    }

    public List<Following> createFollowers(List<String> followersIds) {
        return saveAll(
                followersIds.stream().map(
                        f -> new Following(f, false)).
                        collect(Collectors.toList()));
    }
    @Transactional
    public void setIsNoMore () {
        followingJpaRepository.setAllToIsNoMoreTrue();
        followingJpaRepository.flush();
    }
}
