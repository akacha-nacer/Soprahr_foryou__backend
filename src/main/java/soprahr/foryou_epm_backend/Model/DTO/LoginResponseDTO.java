package soprahr.foryou_epm_backend.Model.DTO;

import lombok.*;
import soprahr.foryou_epm_backend.Model.Role;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Model.maladie.Poste;

import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {

    private Long userID;
    private String firstname;
    private String lastname;
    private String identifiant;
    private String email;
    private Poste poste;
    private Role role;
    private String message;



    public LoginResponseDTO(User user, String message) {
        this.userID = user.getUserID();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.identifiant = user.getIdentifiant();
        this.email = user.getEmail();
        this.poste = user.getPoste();
        this.role = user.getRole();
        this.message = message;
    }
}
