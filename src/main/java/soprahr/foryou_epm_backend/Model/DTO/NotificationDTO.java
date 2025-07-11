package soprahr.foryou_epm_backend.Model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureDeletionRequest;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureModificationRequest;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureRequest;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private boolean retard;
    private boolean cloturee;
    private LocalDateTime createdAt;
    private Long employeeId;
    private String employeeName;
    private String employeeIdentifiant;
    private List<AbsenceDeclarationDTO> absenceDeclarations;
    private NatureHeureRequest natureHeureRequest;
    private NatureHeureModificationRequest natureHeureModificationRequest;
    private NatureHeureDeletionRequest natureHeureDeletionRequest;

}
