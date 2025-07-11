package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureRequest;

import java.util.List;

public interface NatureHeureRequestRepository extends JpaRepository<NatureHeureRequest, Long> {
    @Query("SELECT r FROM NatureHeureRequest r WHERE r.manager.userID = :managerId AND r.status = 'PENDING'")
    List<NatureHeureRequest> findPendingByManagerUserID(Long managerId);
    List<NatureHeureRequest> findAllByUserUserID(Long userId);
}
