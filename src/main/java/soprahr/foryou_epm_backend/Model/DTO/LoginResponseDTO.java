package soprahr.foryou_epm_backend.Model.DTO;

import lombok.*;
import soprahr.foryou_epm_backend.Model.Role;

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
    private Role role;
    private String message;
}
