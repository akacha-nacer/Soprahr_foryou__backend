package soprahr.foryou_epm_backend.Model.Journee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soprahr.foryou_epm_backend.Model.User;


import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NatureHeure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id ;

    String nature_heure ;
    LocalTime heureDebut ;
    LocalTime heureFin ;
    String duree ;
    Boolean isValidee = false ;
    String commentaire ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User user ;
}
