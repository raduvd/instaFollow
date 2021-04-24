package ro.personal.home.instafollow.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.personal.home.instafollow.persistance.model.Followers;
import ro.personal.home.instafollow.persistance.repository.FollowersJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Data
public class FollowerService {

    @Autowired
    private FollowersJpaRepository followerJpaRepository;

    public List<Followers> saveAll(List<Followers> followers) {
        List<Followers> followers1 = followerJpaRepository.saveAll(followers);
        followerJpaRepository.flush();

        return followers1;
    }

    public List<Followers> createFollowers(List<String> followersIds) {
        return saveAll(
                followersIds.stream().map(
                        f -> new Followers(f, false)).
                        collect(Collectors.toList()));
    }

    @Transactional
    public void setIsNoMore() {
        followerJpaRepository.setAllToIsNoMoreTrue();
        followerJpaRepository.flush();
    }
}
