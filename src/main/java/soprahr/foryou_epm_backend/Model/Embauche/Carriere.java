package soprahr.foryou_epm_backend.Model.Embauche;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "carriere")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carriere {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conventionCollective;
    private String accordEntreprise;
    private String qualification;
    private String typePaie;
    private String regimeSpecial;
    private String natureContrat;
    private String typeContrat;
    private String duree;
    private LocalDate dateFinPrevue;
    private LocalDate dateDebutEssai;
    private LocalDate dateFinEssai;
    private String typeTemps;
    private String modaliteHoraire;
    private Boolean forfaitJours;
    private Integer forfaitHeures;
    private String surcotisation;
    private String heuresTravaillees;
    private String heuresPayees;
    private LocalDate dateDebutApprentissage;


    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private CreerLeDossierDUnePersonne dossier;
}
