package soprahr.foryou_epm_backend.Model.Embauche;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nationalite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nationalite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pays;
    private Boolean natPrincipale;

    @ManyToOne
    @JoinColumn(name = "renseignements_id")
    private RenseignementsIndividuels renseignements;
}
