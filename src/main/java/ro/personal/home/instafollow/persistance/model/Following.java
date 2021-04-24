package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "following")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Following implements Serializable {
    @Id
    String id;
    Boolean isNoMore;
}
