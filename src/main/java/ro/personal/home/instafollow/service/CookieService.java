package ro.personal.home.instafollow.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.persistance.repository.CookieJpaRepository;
import ro.personal.home.instafollow.webDriver.webDriver.AppWebDriver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Data
public class CookieService {

    @Autowired
    private CookieJpaRepository cookieJpaRepository;


    public void loadCookies(String user) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy");

        cookieJpaRepository.getCookiesByUserName(user).forEach(c -> {
            Cookie ck = new Cookie(
                    c.getName(),
                    c.getValue(),
                    c.getDomain(),
                    c.getPath(),
                    c.getExpiry() == null ? null : java.sql.Date.valueOf(LocalDate.parse(c.getExpiry(), formatter)),
                    Boolean.valueOf(c.getIsSecure()));

            AppWebDriver.getWebDriver().manage().addCookie(ck);
        });
    }

    public void saveCookies(String user) {

        List<ro.personal.home.instafollow.persistance.model.Cookie> cookieList = new ArrayList<>();

        for (Cookie ck : AppWebDriver.getWebDriver().manage().getCookies()) {

            ro.personal.home.instafollow.persistance.model.Cookie cookie = new ro.personal.home.instafollow.persistance.model.Cookie();
            cookie.setName(ck.getName());
            cookie.setValue(ck.getValue());
            cookie.setDomain(ck.getDomain());
            cookie.setPath(ck.getPath());
            cookie.setExpiry(ck.getExpiry() == null ? null : ck.getExpiry().toString());
            cookie.setIsSecure(String.valueOf(ck.isSecure()));
            cookie.setUserName(user);

            cookieList.add(cookie);
        }

        cookieJpaRepository.deleteAllByUserName(user);
        cookieJpaRepository.saveAll(cookieList);
        cookieJpaRepository.flush();
    }
}
