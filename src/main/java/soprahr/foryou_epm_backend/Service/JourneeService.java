package soprahr.foryou_epm_backend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import soprahr.foryou_epm_backend.Model.Journee.Anomalies;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeure;
import soprahr.foryou_epm_backend.Model.Journee.Pointage;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.AnomaliesRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.PointageRepository;
import soprahr.foryou_epm_backend.Repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JourneeService {

    private final UserRepository userRepository;
    private final AnomaliesRepository anomaliesRepository;
    private final PointageRepository pointageRepository;
    private final NatureHeureRepository natureHeureRepository;




    public List<Pointage> savePointages(List<Pointage> point) {
        return pointageRepository.saveAll(point);
    }

    public Anomalies saveAnomalie(Anomalies anomalies){
        return anomaliesRepository.save(anomalies);
    }

    public NatureHeure saveNatureHeure(NatureHeure natureHeure){
        return natureHeureRepository.save(natureHeure);
    }

    public List<NatureHeure> getAllUserNatureHeures(Long userId){
        return natureHeureRepository.findAllByUserUserID(userId);
    }

    public List<Pointage> getAllPointages(Long userId){
        return pointageRepository.findAllByUserUserID(userId);
    }

    public List<Anomalies> getAllUserAnomalies(Long userId){
        return anomaliesRepository.findAllByUserUserID(userId);
    }

    public NatureHeure getNatureHeureById(Long id) {
        return natureHeureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found with id: " + id));
    }


    public NatureHeure updateNatureHeure(Long id, NatureHeure updatedNatureHeure) {
        NatureHeure existing = getNatureHeureById(id);
        existing.setNature_heure(updatedNatureHeure.getNature_heure());
        existing.setHeureDebut(updatedNatureHeure.getHeureDebut());
        existing.setHeureFin(updatedNatureHeure.getHeureFin());
        existing.setDuree(updatedNatureHeure.getDuree());
        existing.setIsValidee(true);
        existing.setCommentaire(updatedNatureHeure.getCommentaire());
        return natureHeureRepository.save(existing);
    }

    public void deleteNatureHeure(Long id) {
        if (!natureHeureRepository.existsById(id)) {
            throw new IllegalArgumentException("NatureHeure not found with id: " + id);
        }
        natureHeureRepository.deleteById(id);
    }
}
