package soprahr.foryou_epm_backend.Repository.EmbaucheRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Embauche.DepartementNaiss;


@Repository
public interface DepartementRepository extends JpaRepository<DepartementNaiss, Long> {
}
