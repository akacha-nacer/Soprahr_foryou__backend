package soprahr.foryou_epm_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.Adresses;
import soprahr.foryou_epm_backend.Model.Embauche.Affectations;

@Repository
public interface AffectationsRepository extends JpaRepository<Affectations, Long> {

}
