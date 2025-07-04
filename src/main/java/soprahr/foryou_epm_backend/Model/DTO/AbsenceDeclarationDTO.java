package soprahr.foryou_epm_backend.Model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import soprahr.foryou_epm_backend.Model.maladie.Justification;

import java.time.LocalDate;
import java.util.List;

@Data
public class AbsenceDeclarationDTO {
    private Long id;
    private boolean isProlongation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean cloturee;
    @JsonProperty("isValidated")
    private boolean isValidated=false;
    private List<JustificationDTO> justifications;
}
