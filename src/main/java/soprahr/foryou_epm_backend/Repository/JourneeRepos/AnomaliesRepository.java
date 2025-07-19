package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Journee.Anomalies;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeure;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnomaliesRepository extends JpaRepository<Anomalies, Long> {
    List<Anomalies> findAllByUserUserID(Long id);
    List<Anomalies> findByUserUserIDAndDateAnomalie(Long userId, LocalDate dateAnomalie);
}
