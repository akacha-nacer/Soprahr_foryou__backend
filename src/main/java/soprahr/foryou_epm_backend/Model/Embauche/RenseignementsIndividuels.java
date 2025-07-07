package soprahr.foryou_epm_backend.Model.Embauche;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "renseignements_individuels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenseignementsIndividuels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qualite;
    private String nomUsuel;
    private String nomPatronymique;
    private String prenom;
    private String deuxiemePrenom;
    private String sexe;
    private String numeroInsee;
    private LocalDate dateNaissance;
    private String villeNaissance;
    private String paysNaissance;
    private String etatFamilial;
    private LocalDate dateEffet;

    @OneToMany(mappedBy = "renseignements", cascade = CascadeType.ALL)
    private List<Nationalite> nationalites;

    @OneToOne
    @JoinColumn(name = "dossier_id")
    private CreerLeDossierDUnePersonne dossier;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "departement_id")
    private DepartementNaiss departementNaiss;
}
