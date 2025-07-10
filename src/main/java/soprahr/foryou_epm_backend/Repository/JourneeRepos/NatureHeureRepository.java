package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeure;

import java.util.List;

@Repository
public interface NatureHeureRepository extends JpaRepository<NatureHeure, Long> {
    List<NatureHeure> findAllByUserUserID(Long id);
}
