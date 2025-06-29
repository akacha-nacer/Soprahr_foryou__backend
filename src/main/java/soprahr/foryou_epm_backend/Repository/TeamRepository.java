package soprahr.foryou_epm_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
