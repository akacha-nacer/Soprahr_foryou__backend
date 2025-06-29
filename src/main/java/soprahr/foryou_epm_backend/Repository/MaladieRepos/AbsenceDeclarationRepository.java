package soprahr.foryou_epm_backend.Repository.MaladieRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;

import java.util.Optional;

public interface AbsenceDeclarationRepository extends JpaRepository<AbsenceDeclaration, Long> {
    Optional<AbsenceDeclaration> findByEmployeeUserIDAndClotureeFalse(Long employeeId);
}