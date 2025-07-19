package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Journee.ProcessedPointage;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProcessedPointageRepository extends JpaRepository<ProcessedPointage, Long> {
    List<ProcessedPointage> findByUserUserIDAndProcessedDate(Long userId, LocalDate processedDate);
    boolean existsByPointageId(Long pointageId);
}
