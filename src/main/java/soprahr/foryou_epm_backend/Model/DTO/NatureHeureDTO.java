package soprahr.foryou_epm_backend.Model.DTO;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Getter
@Setter
public class NatureHeureDTO {
    private Long id;
    private String nature_heure;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String duree;
    private String commentaire;
    private LocalDate date;
    private String status;
    private Long userid;
}
