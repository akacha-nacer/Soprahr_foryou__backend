package soprahr.foryou_epm_backend.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    String email;
    @Enumerated(EnumType.STRING)
    Role role ;

}
