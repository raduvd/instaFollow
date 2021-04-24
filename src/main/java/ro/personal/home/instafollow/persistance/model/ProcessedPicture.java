package ro.personal.home.instafollow.persistance.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "processedPicture")
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedPicture implements Serializable {

    @Id
    String idPicName;
    Boolean isProcessed;
    String pageName;
}

