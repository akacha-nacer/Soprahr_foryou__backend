package soprahr.foryou_epm_backend.Model.maladie;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Lob
    @Column(name = "Justificatif", nullable = false)
    private byte[] fileContent;
    private boolean originalDepose;
    private boolean accidentTravail;
    private LocalDate dateAccident;

    @ManyToOne
    @JoinColumn(name = "absence_declaration_id", nullable = false)
    private AbsenceDeclaration absenceDeclaration;
}
