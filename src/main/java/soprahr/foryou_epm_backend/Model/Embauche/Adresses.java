package soprahr.foryou_epm_backend.Model.Embauche;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "adresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adresses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pays;
    private String typeAdresse;
    private Boolean adressePrincipale;
    private LocalDate valableDu;
    private LocalDate valableAu;
    private String numeroVoie;
    private String natureVoie;
    private String complement1;
    private String complement2;
    private String lieuDit;
    private String codePostal;
    private String commune;
    private String codeInseeCommune;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private CreerLeDossierDUnePersonne dossier;
}
