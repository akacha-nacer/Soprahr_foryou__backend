package soprahr.foryou_epm_backend.Model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private boolean retard;
    @JsonProperty("isValidated")
    private boolean isValidated=false;
    private boolean cloturee;
    private LocalDateTime createdAt;
    private Long employeeId;
    private String employeeName;
    private List<AbsenceDeclarationDTO> absenceDeclarations;
}
