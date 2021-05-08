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
public class PotentialFollower implements Serializable {

    @Id
    String id;

    private Boolean isFollowRequested;
    private LocalDate followRequestSentAtDate;
    private Boolean followBackRefused;
    private Boolean isRejectedDueToValidation;
    private Integer posts;
    private Integer followers;
    private Integer following;
    private LocalDate removedFromFollowersAtDate;
    private Boolean isAccountPrivate;
    private Boolean removalConfirmed;
    private Boolean followRequestSentConfirmed;
    private Boolean pageCanBeOpened;
}
