package soprahr.foryou_epm_backend.Model.Embauche;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departement_naissance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartementNaiss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code ;
    private String libelle;
    private String status ;

    @OneToOne(mappedBy = "departementNaiss")
    @JsonIgnore
    private RenseignementsIndividuels renseignementsIndividuels;
}
