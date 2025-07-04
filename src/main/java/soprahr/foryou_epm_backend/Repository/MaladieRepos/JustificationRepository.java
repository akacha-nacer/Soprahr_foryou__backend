package soprahr.foryou_epm_backend.Repository.MaladieRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.maladie.Justification;

import java.util.List;

public interface JustificationRepository extends JpaRepository<Justification, Long> {
    List<Justification> findByAbsenceDeclaration_Id(Long id);
}