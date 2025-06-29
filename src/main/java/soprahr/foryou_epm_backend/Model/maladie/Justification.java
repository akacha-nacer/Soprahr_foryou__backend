package soprahr.foryou_epm_backend.Model.maladie;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Justification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String justificatifFileName;
    private boolean originalDepose;
    private boolean accidentTravail;
    private LocalDate dateAccident;

    @ManyToOne
    @JoinColumn(name = "absence_declaration_id", nullable = false)
    private AbsenceDeclaration absenceDeclaration;
}
