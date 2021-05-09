package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Entity(name = "results")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result implements Serializable {

    @Id
    LocalDateTime addedAt;
    String processType;
    AtomicInteger followed = new AtomicInteger(0);
    AtomicInteger removed = new AtomicInteger(0);
    AtomicInteger confirmedRemoved = new AtomicInteger(0);
    AtomicInteger confirmedFollowing = new AtomicInteger(0);
}
