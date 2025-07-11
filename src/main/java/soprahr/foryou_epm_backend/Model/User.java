package soprahr.foryou_epm_backend.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import soprahr.foryou_epm_backend.Model.maladie.Poste;

@Entity
@Getter
@Builder
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"team"})
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userID;

    @Column(nullable = false)
    String password;

    String firstname;
    String lastname;
    String identifiant;
    @Enumerated(EnumType.STRING)
    Poste poste;
    String email;
    @Lob
    @Column(name = "profile_picture")
    @JsonIgnore
    byte[] profilePicture;
    @Enumerated(EnumType.STRING)
    Role role ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
