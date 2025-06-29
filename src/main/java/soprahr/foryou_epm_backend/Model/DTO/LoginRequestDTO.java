package soprahr.foryou_epm_backend.Model.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {

    private String identifiant;
    private String password;
}
