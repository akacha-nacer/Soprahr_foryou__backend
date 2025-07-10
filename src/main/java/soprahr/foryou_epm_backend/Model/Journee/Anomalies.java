package soprahr.foryou_epm_backend.Model.Journee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soprahr.foryou_epm_backend.Model.User;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anomalies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id ;

    LocalDate dateAnomalie ;
    String details ;
    Integer poid ;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User user ;
}
