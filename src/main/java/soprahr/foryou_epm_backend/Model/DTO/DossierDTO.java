package soprahr.foryou_epm_backend.Model.DTO;

import lombok.Data;
import soprahr.foryou_epm_backend.Model.Embauche.*;

import java.time.LocalDate;
import java.util.List;

@Data
public class DossierDTO {
    private LocalDate dateRecrutement;
    private String codeSociete;
    private String etablissement;
    private String matriculeSalarie;
    private Long departementId;

    private RenseignementsIndividuels renseignementsIndividuels;
    private List<Adresses> adresses;
    private List<Affectations> affectations;
    private List<Carriere> carriere;



}
