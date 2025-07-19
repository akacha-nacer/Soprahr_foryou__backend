package soprahr.foryou_epm_backend.Repository.JourneeRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureDeletionRequest;

import java.util.List;

public interface NatureHeureDeletionRequestRepository extends JpaRepository<NatureHeureDeletionRequest, Long> {
    List<NatureHeureDeletionRequest> findByApprovedFalseAndRejectedFalseAndRequestedByTeamManagerUserID(long id);
    List<NatureHeureDeletionRequest> findAllByRequestedBy_UserID(Long id);
}
