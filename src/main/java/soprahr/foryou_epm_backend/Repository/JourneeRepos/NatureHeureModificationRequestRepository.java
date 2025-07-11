package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureModificationRequest;

import java.util.List;

public interface NatureHeureModificationRequestRepository extends JpaRepository<NatureHeureModificationRequest,Long> {
    List<NatureHeureModificationRequest> findByApprovedFalseAndRejectedFalseAndRequestedByTeamManagerUserID(Long managerId);
}
