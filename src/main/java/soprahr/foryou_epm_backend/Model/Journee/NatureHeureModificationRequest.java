package soprahr.foryou_epm_backend.Model.Journee;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soprahr.foryou_epm_backend.Model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NatureHeureModificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String newNatureHeure;
    private LocalTime newHeureDebut;
    private LocalTime newHeureFin;
    private String newDuree;
    private String newCommentaire;
    LocalDate newDate = LocalDate.now();

    private boolean approved = false;
    private boolean rejected = false;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private Long requestedById ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nature_heure_id")
    private NatureHeure originalNatureHeure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User requestedBy;
}
