package soprahr.foryou_epm_backend.Repository.MaladieRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;

public interface AbsenceDeclarationRepository extends JpaRepository<AbsenceDeclaration, Long> {
}