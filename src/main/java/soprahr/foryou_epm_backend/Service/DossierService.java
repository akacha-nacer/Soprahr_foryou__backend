package soprahr.foryou_epm_backend.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soprahr.foryou_epm_backend.Model.DTO.DossierDTO;
import soprahr.foryou_epm_backend.Model.Embauche.*;
import soprahr.foryou_epm_backend.Repository.*;

@Service
public class DossierService {
    @Autowired
    private CreerLeDossierDUnePersonneRepository dossierRepository;
    @Autowired
    private RenseignementsIndividuelsRepository renseignementsRepository;
    @Autowired
    private AdressesRepository adressesRepository;
    @Autowired
    private AffectationsRepository affectationsRepository;
    @Autowired
    private CarriereRepository carriereRepository;
    @Autowired
    private NationaliteRepository nationaliteRepository;

    @Transactional
    public void saveDossier(DossierDTO dossierDTO) {
        CreerLeDossierDUnePersonne dossier = new CreerLeDossierDUnePersonne();
        dossier.setDateRecrutement(dossierDTO.getDateRecrutement());
        dossier.setCodeSociete(dossierDTO.getCodeSociete());
        dossier.setEtablissement(dossierDTO.getEtablissement());
        dossier.setMatriculeSalarie(dossierDTO.getMatriculeSalarie());
        dossier = dossierRepository.save(dossier);

        RenseignementsIndividuels renseignements = dossierDTO.getRenseignementsIndividuels();
        if (renseignements != null) {
            renseignements.setDossier(dossier);
            renseignements = renseignementsRepository.save(renseignements);

            if (renseignements.getNationalites() != null) {
                for (Nationalite nationalite : renseignements.getNationalites()) {
                    nationalite.setRenseignements(renseignements);
                    nationaliteRepository.save(nationalite);
                }
            }
        }


        if (dossierDTO.getAdresses() != null) {
            for (Adresses adresse : dossierDTO.getAdresses()) {
                adresse.setDossier(dossier);
                adressesRepository.save(adresse);
            }
        }

        if (dossierDTO.getAffectations() != null) {
            for (Affectations affectation : dossierDTO.getAffectations()) {
                affectation.setDossier(dossier);
                affectationsRepository.save(affectation);
            }
        }

        if (dossierDTO.getCarriere() != null) {
            for (Carriere carriere : dossierDTO.getCarriere()) {
                carriere.setDossier(dossier);
                carriereRepository.save(carriere);
            }
        }
    }
}
