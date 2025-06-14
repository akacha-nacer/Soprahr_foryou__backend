package soprahr.foryou_epm_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.Adresses;

@Repository
public interface AdressesRepository extends JpaRepository<Adresses, Long> {

}
