package soprahr.foryou_epm_backend.Model.Embauche;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soprahr.foryou_epm_backend.Model.User;

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
    private LocalDate dateCreation = LocalDate.now();

    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "dossier-renseignements")
    private RenseignementsIndividuels renseignementsIndividuels;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "dossier-adresses")
    private List<Adresses> adresses;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "dossier-affectations")
    private List<Affectations> affectations;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "dossier-carriere")
    private List<Carriere> carriere;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User employee;
}
