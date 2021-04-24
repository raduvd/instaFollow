package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "followers")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Followers implements Serializable {
    @Id
    String id;
    Boolean isNoMore;
}
