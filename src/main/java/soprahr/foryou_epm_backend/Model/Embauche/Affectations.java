package soprahr.foryou_epm_backend.Model.Embauche;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "affectations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Affectations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categorieEntree;
    private String motifEntree;
    private String poste;
    private String emploi;
    private String uniteOrganisationnelle;
    private String calendrierPaie;
    private String codeCycle;
    private Boolean indexe;
    private String modaliteGestion;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private CreerLeDossierDUnePersonne dossier;
}
