package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;

@Entity(name = "potentialFollowers")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotentialFollower implements Serializable{

    @Id
    String id;

    private Boolean isFollower;
    private Boolean isFollowRequested;
    private LocalDate followRequestSentAtDate;
    private Boolean followBackRefused;
    private Boolean isRejectedDueToValidation;
    private Integer posts;
    private Integer followers;
    private Integer following;

    public PotentialFollower(String f, boolean b, boolean b1, Object o, boolean b2, boolean b3, Object o1, Object o2, Object o3) {
        this.id = f;

    }
}
