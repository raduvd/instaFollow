package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "account")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    @Id
    String usrnam;
    String pass;
}
