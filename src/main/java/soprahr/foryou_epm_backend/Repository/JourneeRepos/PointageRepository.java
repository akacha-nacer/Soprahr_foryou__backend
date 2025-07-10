package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeure;
import soprahr.foryou_epm_backend.Model.Journee.Pointage;

import java.util.List;


@Repository
public interface PointageRepository extends JpaRepository<Pointage, Long> {
    List<Pointage> findAllByUserUserID(Long id);
}
