package soprahr.foryou_epm_backend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soprahr.foryou_epm_backend.Model.DTO.NatureHeureDTO;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.Journee.*;
import soprahr.foryou_epm_backend.Model.Role;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.*;
import soprahr.foryou_epm_backend.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JourneeService {

    private final UserRepository userRepository;
    private final AnomaliesRepository anomaliesRepository;
    private final PointageRepository pointageRepository;
    private final NatureHeureRepository natureHeureRepository;
    private final NatureHeureRequestRepository natureHeureRequestRepository;
    private final NatureHeureDeletionRequestRepository deletionRequestRepository;
    private final NatureHeureModificationRequestRepository modificationRequestRepository; // Added





    public List<Pointage> savePointages(List<Pointage> point) {
        return pointageRepository.saveAll(point);
    }

    public Anomalies saveAnomalie(Anomalies anomalies){
        return anomaliesRepository.save(anomalies);
    }

    public NatureHeureRequest saveNatureHeure(NatureHeure natureHeure, Long userId) {
        // Fetch user and validate role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.getRole() == Role.MANAGER) {
            // Managers can directly save NatureHeure
            natureHeure.setUser(user);
            natureHeure.setIsValidee(true);
            return convertToRequest(natureHeureRepository.save(natureHeure), "APPROVED", user, user);
        }

        // Validate required fields for non-managers
        if (natureHeure.getNature_heure() == null || natureHeure.getNature_heure().isBlank()) {
            throw new IllegalArgumentException("Nature_heure is required");
        }
        if (natureHeure.getHeureDebut() == null) {
            throw new IllegalArgumentException("HeureDebut is required");
        }
        if (natureHeure.getHeureFin() == null) {
            throw new IllegalArgumentException("HeureFin is required");
        }
        if (!natureHeure.getHeureFin().isAfter(natureHeure.getHeureDebut())) {
            throw new IllegalArgumentException("HeureFin must be after HeureDebut");
        }

        // Calculate duree
        long minutes = java.time.Duration.between(natureHeure.getHeureDebut(), natureHeure.getHeureFin()).toMinutes();
        if (minutes <= 0) {
            throw new IllegalArgumentException("Invalid duration: HeureFin must be after HeureDebut");
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        natureHeure.setDuree(String.format("%dh%02d", hours, remainingMinutes));



        NatureHeureRequest request = new NatureHeureRequest();
        request.setNature_heure(natureHeure.getNature_heure());
        request.setHeureDebut(natureHeure.getHeureDebut());
        request.setHeureFin(natureHeure.getHeureFin());
        request.setDuree(natureHeure.getDuree());
        request.setCommentaire(natureHeure.getCommentaire());
        request.setDate(natureHeure.getDate());
        request.setManager(user.getTeam().getManager());
        request.setUser(user);
        request.setUserid(userId);
        request.setStatus("PENDING");


        return natureHeureRequestRepository.save(request);
    }

    public List<NatureHeureDTO> getPendingModificationRequests(Long managerId) {
       List<NatureHeureModificationRequest> natureHeureModificationRequests = modificationRequestRepository.findByApprovedFalseAndRejectedFalseAndRequestedByTeamManagerUserID(managerId);
        return natureHeureModificationRequests.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    private NatureHeureDTO mapToDTO(NatureHeureModificationRequest request) {
        NatureHeureDTO dto = new NatureHeureDTO();
        dto.setId(request.getId());
        dto.setNature_heure(request.getNewNatureHeure());
        dto.setHeureDebut(request.getNewHeureDebut());
        dto.setHeureFin(request.getNewHeureFin());
        dto.setDuree(request.getNewDuree());
        dto.setCommentaire(request.getNewCommentaire());
        dto.setDate(request.getNewDate());
        dto.setStatus(request.isApproved() ? "APPROVED" : request.isRejected() ? "REJECTED" : "PENDING");
        dto.setUserid(request.getRequestedBy() != null ? request.getRequestedBy().getUserID() : null);
        return dto;
    }
    public void rejectNatureHeureRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can reject requests");
        }

        NatureHeureRequest request = natureHeureRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus("REJECTED");
        natureHeureRequestRepository.save(request);
    }

    public NatureHeure approveNatureHeureRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can approve requests");
        }

        NatureHeureRequest request = natureHeureRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is not pending");
        }

        NatureHeure natureHeure = new NatureHeure();
        natureHeure.setNature_heure(request.getNature_heure());
        natureHeure.setHeureDebut(request.getHeureDebut());
        natureHeure.setHeureFin(request.getHeureFin());
        natureHeure.setDuree(request.getDuree());
        natureHeure.setCommentaire(request.getCommentaire());
        natureHeure.setDate(request.getDate());
        natureHeure.setUser(request.getUser());
        natureHeure.setIsValidee(true);

        // Save NatureHeure and update request status
        NatureHeure saved = natureHeureRepository.save(natureHeure);
        request.setStatus("APPROVED");
        natureHeureRequestRepository.save(request);

        return saved;
    }

    public List<NatureHeureRequest> getPendingRequests(Long managerId) {
        return natureHeureRequestRepository.findPendingByManagerUserID(managerId);
    }

    @Transactional(readOnly = true)
    public List<NatureHeure> getAllUserNatureHeures(Long userId){
        return natureHeureRepository.findAllByUserUserID(userId);
    }

    public List<Pointage> getAllPointages(Long userId){
        return pointageRepository.findAllByUserUserID(userId);
    }

    public List<Anomalies> getAllUserAnomalies(Long userId){
        return anomaliesRepository.findAllByUserUserID(userId);
    }

    public NatureHeure getNatureeHeureById(Long id) {
        return natureHeureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found with id: " + id));
    }


    public NatureHeure updateNatureHeure(Long id, NatureHeure updatedNatureHeure) {
        NatureHeure existing = getNatureeHeureById(id);
        existing.setNature_heure(updatedNatureHeure.getNature_heure());
        existing.setHeureDebut(updatedNatureHeure.getHeureDebut());
        existing.setHeureFin(updatedNatureHeure.getHeureFin());
        existing.setDuree(updatedNatureHeure.getDuree());
        existing.setIsValidee(true);
        existing.setCommentaire(updatedNatureHeure.getCommentaire());
        return natureHeureRepository.save(existing);
    }



    public void deleteNatureHeure(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        NatureHeure natureHeure = natureHeureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found with id: " + id));

        if (user.getRole() == Role.MANAGER) {
            natureHeureRepository.deleteById(id);
        } else {
            NatureHeureDeletionRequest request = new NatureHeureDeletionRequest();
            request.setOriginalNatureHeure(natureHeure);
            request.setRequestedBy(user);
            request.setApproved(false);
            request.setRejected(false);
            deletionRequestRepository.save(request);
        }
    }

    public void approveDeletionRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can approve deletion requests");
        }

        NatureHeureDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found with id: " + requestId));
        if (request.isApproved() || request.isRejected()) {
            throw new IllegalStateException("Deletion request is already processed");
        }

        NatureHeure natureHeure = request.getOriginalNatureHeure();
        if (natureHeure == null) {
            throw new IllegalArgumentException("Original NatureHeure not found for request ID: " + requestId);
        }
        natureHeure.setUser(null);
        request.setOriginalNatureHeure(null);
        natureHeureRepository.delete(natureHeure);
        request.setApproved(true);
        request.setRejected(false);
        deletionRequestRepository.save(request);
    }

    public void rejectDeletionRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can reject deletion requests");
        }

        NatureHeureDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found with id: " + requestId));
        if (request.isApproved() || request.isRejected()) {
            throw new IllegalStateException("Deletion request is already processed");
        }

        request.setApproved(false);
        request.setRejected(true);
        deletionRequestRepository.save(request);
    }

    private NatureHeureRequest convertToRequest(NatureHeure natureHeure, String status, User user, User manager) {
        NatureHeureRequest request = new NatureHeureRequest();
        request.setNature_heure(natureHeure.getNature_heure());
        request.setHeureDebut(natureHeure.getHeureDebut());
        request.setHeureFin(natureHeure.getHeureFin());
        request.setDuree(natureHeure.getDuree());
        request.setCommentaire(natureHeure.getCommentaire());
        request.setDate(natureHeure.getDate());
        request.setUser(user);
        request.setManager(manager);
        request.setStatus(status);
        return request;
    }

    public List<NatureHeureDeletionRequest> getPendingDeletionRequests(Long managerId) {
        return deletionRequestRepository.findAll().stream()
                .filter(r -> !r.isApproved() && !r.isRejected() && r.getRequestedBy().getTeam().getManager().getUserID().equals(managerId))
                .collect(Collectors.toList());
    }


    public List<NotificationDTO> getNatureHeureNotifications(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can view notifications");
        }

        List<NotificationDTO> notifications = new ArrayList<>();

        // Fetch pending NatureHeure requests
        List<NatureHeureRequest> addRequests = natureHeureRequestRepository.findPendingByManagerUserID(managerId);
        notifications.addAll(addRequests.stream().map(request -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(request.getId());
            dto.setEmployeeId(request.getUser().getUserID());
            dto.setEmployeeName(request.getUser().getFirstname());
            dto.setEmployeeIdentifiant(request.getUser().getIdentifiant());
            dto.setCreatedAt(request.getDate().atStartOfDay());
            dto.setMessage("Demande d'ajout de nature d'heure: " + request.getNature_heure());
            dto.setNatureHeureRequest(request);
            return dto;
        }).collect(Collectors.toList()));

        // Fetch pending modification requests
        List<NatureHeureModificationRequest> modificationRequests = modificationRequestRepository
                .findByApprovedFalseAndRejectedFalseAndRequestedByTeamManagerUserID(managerId);
        notifications.addAll(modificationRequests.stream().map(request -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(request.getId());
            dto.setEmployeeId(request.getRequestedBy().getUserID());
            dto.setEmployeeName(request.getRequestedBy().getFirstname());
            dto.setEmployeeIdentifiant(request.getRequestedBy().getIdentifiant());
            dto.setCreatedAt(request.getRequestedAt());
            dto.setMessage("Demande de modification de nature d'heure: " + request.getNewNatureHeure());
            dto.setNatureHeureModificationRequest(request);
            return dto;
        }).collect(Collectors.toList()));

        // Fetch pending deletion requests
        List<NatureHeureDeletionRequest> deletionRequests = deletionRequestRepository.findAll().stream()
                .filter(r -> !r.isApproved() && !r.isRejected() && r.getRequestedBy().getTeam().getManager().getUserID().equals(managerId))
                .collect(Collectors.toList());
        notifications.addAll(deletionRequests.stream().map(request -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(request.getId());
            dto.setEmployeeId(request.getRequestedBy().getUserID());
            dto.setEmployeeName(request.getRequestedBy().getFirstname());
            dto.setEmployeeIdentifiant(request.getRequestedBy().getIdentifiant());
            dto.setCreatedAt(LocalDateTime.now()); // Use actual requestedAt if available
            dto.setMessage("Demande de suppression de nature d'heure: " + request.getOriginalNatureHeure().getNature_heure());
            dto.setNatureHeureDeletionRequest(request);
            return dto;
        }).collect(Collectors.toList()));

        return notifications;
    }




}
