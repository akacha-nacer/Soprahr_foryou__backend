package soprahr.foryou_epm_backend.Model.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class NatureHeureDeleteDTO {
    private Long id;
    private boolean approved;
    private boolean rejected;
    private Long requestedById;
}
