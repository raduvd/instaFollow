package ro.personal.home.instafollow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.personal.home.instafollow.persistance.model.Account;
import ro.personal.home.instafollow.persistance.repository.AccountJpaRepository;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    public Account saveAccount(String usrnm, String pass) {

        return accountJpaRepository.save(new Account(encode(usrnm), encode(pass)));
    }

    public Pair<String, String> getAccount(String usrnm) {

        Account one = accountJpaRepository.getOne(usrnm);

        return Pair.of(decode(one.getUsrnam()), decode(one.getPass()));
    }

    public String encode(String str) {
        System.out.println("ENCODING STRING: " + str);
        byte[] bytesEncoded = Base64.getEncoder().encode(str.getBytes());
        return new String(bytesEncoded);
    }

    public String decode(String str) {
        System.out.println("DECODING STRING: " + str);
        byte[] valueDecoded = Base64.getDecoder().decode(str);
        return new String(valueDecoded);
    }
}
