package ro.personal.home.instafollow.persistance.specification;

import org.springframework.data.jpa.domain.Specification;
import ro.personal.home.instafollow.enums.Process;
import ro.personal.home.instafollow.persistance.model.ProcessResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SpecificationUtil {

    public static Specification<ProcessResult> getProcessResultsByProcessType(Process process) {
        return (r, g, c) -> c.equal(r.get("processType"), process.toString());
    }

    public static Specification<ProcessResult> getProcessResultsInNumberOfDays(Integer numberOfDaysInterval) {
        LocalTime midnight = LocalTime.MIDNIGHT.plusHours(1);
        LocalDate today = LocalDate.now();
        LocalDateTime intervalOfDaysMidnight = LocalDateTime.of(today.minusDays(numberOfDaysInterval), midnight);

        return (r, g, c) -> c.greaterThan(r.get("addedAt"), intervalOfDaysMidnight);
    }
}
