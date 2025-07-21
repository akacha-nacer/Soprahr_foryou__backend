package soprahr.foryou_epm_backend.Repository.EmbaucheRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.CreerLeDossierDUnePersonne;

import java.util.List;

@Repository
public interface CreerLeDossierDUnePersonneRepository extends JpaRepository<CreerLeDossierDUnePersonne, Long> {

    List<CreerLeDossierDUnePersonne> findAllByEmployee_UserID(Long id);
}
