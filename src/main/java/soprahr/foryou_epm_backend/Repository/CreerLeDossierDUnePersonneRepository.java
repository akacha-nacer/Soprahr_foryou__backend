package soprahr.foryou_epm_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.CreerLeDossierDUnePersonne;

@Repository
public interface CreerLeDossierDUnePersonneRepository extends JpaRepository<CreerLeDossierDUnePersonne, Long> {

}
