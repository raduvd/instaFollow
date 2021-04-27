package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "cookie")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cookie implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;
    String value;
    String domain;
    String path;
    String expiry;
    String isSecure;
    String userName;
}
