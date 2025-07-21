package soprahr.foryou_epm_backend.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soprahr.foryou_epm_backend.Model.DTO.DossierDTO;
import soprahr.foryou_epm_backend.Model.Embauche.*;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.EmbaucheRepos.*;

import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private DepartementRepository departementRepository;


    @Transactional(readOnly = true)
    public List<DossierDTO> getAllDossier() {
        List<CreerLeDossierDUnePersonne> dossiers = dossierRepository.findAll();

        return dossiers.stream().map(dossier -> {
            DossierDTO dto = new DossierDTO();

            dto.setDateRecrutement(dossier.getDateRecrutement());
            dto.setCodeSociete(dossier.getCodeSociete());
            dto.setEtablissement(dossier.getEtablissement());
            dto.setMatriculeSalarie(dossier.getMatriculeSalarie());
            dto.setDateCreation(dossier.getDateCreation());


            RenseignementsIndividuels renseignements = dossier.getRenseignementsIndividuels();
            if (renseignements != null) {

                renseignements.getNationalites().size();
                dto.setRenseignementsIndividuels(renseignements);


                DepartementNaiss departement = renseignements.getDepartementNaiss();
                if (departement != null) {
                    dto.setDepartementId(departement.getId());
                    dto.setDepartementLibelle(departement.getLibelle());
                }
            }


            dto.setAdresses(dossier.getAdresses());
            dto.setAffectations(dossier.getAffectations());
            dto.setCarriere(dossier.getCarriere());

            return dto;
        }).collect(Collectors.toList());
    }


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

            if (dossierDTO.getDepartementId() != null) {
                DepartementNaiss departement = departementRepository.findById(dossierDTO.getDepartementId())
                        .orElseThrow(() -> new RuntimeException("Departement not found with ID: " + dossierDTO.getDepartementId()));
                renseignements.setDepartementNaiss(departement);
            }
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

    public List<DepartementNaiss> saveDepartementNaiss(List<DepartementNaiss> dep) {
        return departementRepository.saveAll(dep);
    }

    public List<DepartementNaiss> RetrieveDepartementNaiss() {
        return departementRepository.findAll();
    }
}
