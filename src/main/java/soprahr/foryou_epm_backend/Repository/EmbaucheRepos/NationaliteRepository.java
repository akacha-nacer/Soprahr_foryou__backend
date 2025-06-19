package soprahr.foryou_epm_backend.Repository.EmbaucheRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.Nationalite;

@Repository
public interface NationaliteRepository extends JpaRepository<Nationalite, Long> {

}
