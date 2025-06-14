package soprahr.foryou_epm_backend.Model.Embauche;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Entity
@Table(name = "creer_le_dossier_d_une_personne")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreerLeDossierDUnePersonne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateRecrutement;
    private String codeSociete;
    private String etablissement;
    private String matriculeSalarie;

    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
    private RenseignementsIndividuels renseignementsIndividuels;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    private List<Adresses> adresses;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    private List<Affectations> affectations;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    private List<Carriere> carriere;
}
