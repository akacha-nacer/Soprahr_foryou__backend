package soprahr.foryou_epm_backend.Model.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AbsenceDeclarationDTO {
    private Long id;
    private boolean isProlongation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean cloturee;
}
