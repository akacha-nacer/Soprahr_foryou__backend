package soprahr.foryou_epm_backend.Model.Journee;

import jakarta.persistence.*;
import lombok.Data;
import soprahr.foryou_epm_backend.Model.User;

@Entity
@Data
public class NatureHeureDeletionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean approved;
    private boolean rejected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nature_heure_id")
    private NatureHeure originalNatureHeure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id")
    private User requestedBy;

}
