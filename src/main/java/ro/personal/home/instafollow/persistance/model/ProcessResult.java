package ro.personal.home.instafollow.persistance.model;

import lombok.*;
import ro.personal.home.instafollow.enums.Process;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Entity(name = "processResult")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResult implements Serializable {

    /**
     * Creates a {@link ProcessResult} with today's date
     *
     * @param process process type
     */
    public ProcessResult(Process process) {
        this.addedAt = LocalDateTime.now();
        this.processType = process.toString();
    }

    @Id
    LocalDateTime addedAt;
    String processType;
    Integer followed = Integer.valueOf(0);
    Integer removed = Integer.valueOf(0);
    Integer confirmedRemoved = Integer.valueOf(0);
    Integer confirmedFollowing = Integer.valueOf(0);
    Integer totalProcessedUsers = Integer.valueOf(0);
    Integer fromTotalWeAppliedLogicOn = Integer.valueOf(0);
    @Transient
    Set<String> processedUserList = new TreeSet<>();

    public Integer incrementAndGetFollowed() {
        return ++followed;
    }

    public Integer incrementAndGetRemoved() {
        return ++removed;
    }

    public Integer incrementAndGetConfirmedRemoved() {
        return ++confirmedRemoved;
    }

    public Integer incrementAndGetConfirmedFollowing() {
        return ++confirmedFollowing;
    }

    public Integer decrementAndGetFollowed() {
        return --followed;
    }

    public Integer decrementAndGetRemoved() {
        return --removed;
    }

    public Integer decrementAndGetConfirmedRemoved() {
        return --confirmedRemoved;
    }

    public Integer decrementAndConfirmedFollowing() {
        return -confirmedFollowing;
    }

    @Override
    public String toString() {
        switch (this.processType) {
            case "REFRESH_FOLLOWERS":
                return "REFRESH_FOLLOWERS process has found followers --> " + this.totalProcessedUsers;
            case "REMOVING_OR_FOLLOWING":
                return "REMOVING_OR_FOLLOWING process (" + this.totalProcessedUsers + ") has removed (" + this.removed +
                        ") and followed (" + this.followed + ")";
            case "FOLLOWING":
                return "FOLLOWING process has followed (" + this.followed + ")";
            case "REMOVE_NON_FOLLOWERS":
                return "REMOVE_NON_FOLLOWERS process has removed (" + this.fromTotalWeAppliedLogicOn + "). " +
                        "And we processed: " + this.totalProcessedUsers;
            case "GET_NEW_POTENTIAL_FOLLOWERS":
                return "GET_NEW_POTENTIAL_FOLLOWERS process has hovered and added in DB for one pic (" + this.fromTotalWeAppliedLogicOn + ")";
            default:
                return "";
        }
    }
}
