package soprahr.foryou_epm_backend.Model.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class JustificationDTO {
    private Long id;
    private boolean originalDepose;
    private boolean accidentTravail;
    private LocalDate dateAccident;
}
