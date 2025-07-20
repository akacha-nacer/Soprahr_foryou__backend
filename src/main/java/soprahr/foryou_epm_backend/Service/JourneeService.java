package soprahr.foryou_epm_backend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soprahr.foryou_epm_backend.Model.DTO.NatureHeureDTO;
import soprahr.foryou_epm_backend.Model.DTO.NatureHeureDeleteDTO;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.Journee.*;
import soprahr.foryou_epm_backend.Model.Role;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.*;
import soprahr.foryou_epm_backend.Repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final NatureHeureModificationRequestRepository modificationRequestRepository;
    private final ProcessedPointageRepository processedPointageRepository;





    public List<Pointage> savePointages(List<Pointage> point) {
        return pointageRepository.saveAll(point);
    }

    public Anomalies saveAnomalie(Anomalies anomalies){
        return anomaliesRepository.save(anomalies);
    }

    @Transactional
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

    public void rejectNatureHeureModifRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can reject requests");
        }

        NatureHeureModificationRequest request = modificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));


        request.setRejected(true);
        request.setApproved(false);
        modificationRequestRepository.save(request);
    }

    public NatureHeure approveNatureHeureModifRequest(Long requestId, Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can approve requests");
        }

        NatureHeureModificationRequest request = modificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));

        User sender = userRepository.findById(request.getRequestedById())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));

        NatureHeure natureHeure = natureHeureRepository.findById(request.getOriginalNatureHeure().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + requestId));
        natureHeure.setNature_heure(request.getNewNatureHeure());
        natureHeure.setHeureDebut(request.getNewHeureDebut());
        natureHeure.setHeureFin(request.getNewHeureFin());
        natureHeure.setDuree(request.getNewDuree());
        natureHeure.setCommentaire(request.getNewCommentaire());
        natureHeure.setDate(request.getNewDate());
        natureHeure.setUser(sender);
        natureHeure.setIsValidee(true);

        NatureHeure saved = natureHeureRepository.save(natureHeure);
        request.setApproved(true);
        request.setRejected(false);
        modificationRequestRepository.save(request);

        return saved;
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
        List<NatureHeureModificationRequest> natureHeureModificationRequest = modificationRequestRepository.findByOriginalNatureHeureId(natureHeure.getId());
        if (natureHeureModificationRequest != null) {
            natureHeureModificationRequest.stream().forEach(natmodifreq -> {
                natmodifreq.setOriginalNatureHeure(null);
                modificationRequestRepository.save(natmodifreq);
            });
        }
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
        request.setOriginalNatureHeure(null);
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

    public List<NatureHeureDeleteDTO> getPendingDeletionRequests(Long managerId) {
        List<NatureHeureDeletionRequest> natureHeureDeletionRequests = deletionRequestRepository.findByApprovedFalseAndRejectedFalseAndRequestedByTeamManagerUserID(managerId);
        return  natureHeureDeletionRequests.stream().map(this::mapToDTODel).collect(Collectors.toList());

    }

    private NatureHeureDeleteDTO mapToDTODel(NatureHeureDeletionRequest request) {
        NatureHeureDeleteDTO dto = new NatureHeureDeleteDTO();
        dto.setId(request.getId());
        dto.setId(request.getId());
        dto.setApproved(request.isApproved());
        dto.setRejected(request.isRejected());
        dto.setRequestedById(request.getRequestedBy().getUserID());
        return dto;
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



    @Transactional
    public List<Anomalies> generateAnomaliesForUser(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Fetch unprocessed pointages for the user and date
        List<Pointage> pointages = pointageRepository.findAllByUserUserIDAndCreatedAt(userId, date)
                .stream()
                .filter(p -> !processedPointageRepository.existsByPointageId(p.getId()))
                .sorted((p1, p2) -> p1.getHeure().compareTo(p2.getHeure()))
                .collect(Collectors.toList());

        log.info("Found {} unprocessed pointages for user {} on date {}", pointages.size(), userId, date);
        pointages.forEach(p -> log.info("Pointage ID: {}, Sens: {}, Heure: {}", p.getId(), p.getSens(), p.getHeure()));

        List<Anomalies> anomalies = new ArrayList<>();
        List<ProcessedPointage> processedPointages = new ArrayList<>();

        if (pointages.isEmpty()) {
            log.info("No unprocessed pointages for user {} on date {}", userId, date);
            return anomalies;
        }

        // Check for existing anomalies to avoid duplicates
        List<Anomalies> existingAnomalies = anomaliesRepository.findByUserUserIDAndDateAnomalie(userId, date);
        Set<String> existingDetails = existingAnomalies.stream()
                .map(Anomalies::getDetails)
                .collect(Collectors.toSet());

        // Check for consecutive entries of the same type
        for (int i = 0; i < pointages.size() - 1; i++) {
            Pointage current = pointages.get(i);
            Pointage next = pointages.get(i + 1);
            if (current.getSens() == next.getSens()) {
                String anomalyDetail = String.format("Consecutive %s pointages at %s and %s",
                        current.getSens(), current.getHeure(), next.getHeure());
                if (!existingDetails.contains(anomalyDetail)) {
                    Anomalies anomaly = new Anomalies();
                    anomaly.setDateAnomalie(date);
                    anomaly.setDetails(anomalyDetail);
                    anomaly.setPoid(3);
                    anomaly.setUser(user);
                    anomalies.add(anomaly);
                    existingDetails.add(anomalyDetail); // Prevent duplicates in this run
                } else {
                    log.info("Skipping duplicate anomaly: {}", anomalyDetail);
                }
            }
        }

        // Check for unmatched entry/exit only after 6 PM for current day
        LocalDate today = LocalDate.now();
        boolean checkEntryExit = !date.isEqual(today) || LocalTime.now().isAfter(LocalTime.of(18, 0));
        if (checkEntryExit) {
            long entryCount = pointages.stream().filter(p -> p.getSens() == Sens.ENTREE).count();
            long exitCount = pointages.stream().filter(p -> p.getSens() == Sens.SORTIE).count();
            if (entryCount != exitCount) {
                String anomalyDetail = String.format("Mismatch in entry/exit counts: %d entries, %d exits", entryCount, exitCount);
                if (!existingDetails.contains(anomalyDetail)) {
                    Anomalies anomaly = new Anomalies();
                    anomaly.setDateAnomalie(date);
                    anomaly.setDetails(anomalyDetail);
                    anomaly.setPoid(4);
                    anomaly.setUser(user);
                    anomalies.add(anomaly);
                    existingDetails.add(anomalyDetail);
                } else {
                    log.info("Skipping duplicate anomaly: {}", anomalyDetail);
                }
            }
        } else {
            log.info("Skipping entry/exit count anomaly check for current day {} until after 6 PM", date);
        }

        // Mark pointages as processed before saving anomalies
        for (Pointage pointage : pointages) {
            ProcessedPointage processed = new ProcessedPointage();
            processed.setPointage(pointage);
            processed.setUser(user);
            processed.setProcessedDate(date);
            processedPointages.add(processed);
        }
        processedPointageRepository.saveAll(processedPointages);
        log.info("Marked {} pointages as processed for user {} on date {}", processedPointages.size(), userId, date);

        // Save anomalies
        if (!anomalies.isEmpty()) {
            anomaliesRepository.saveAll(anomalies);
            log.info("Generated and saved {} new anomalies for user {} on date {}", anomalies.size(), userId, date);
        }

        return anomalies;
    }

    @Transactional
    public List<Anomalies> generateAnomaliesForAllUsers(LocalDate date) {
        List<User> users = userRepository.findAll();
        List<Anomalies> allAnomalies = new ArrayList<>();
        for (User user : users) {
            allAnomalies.addAll(generateAnomaliesForUser(user.getUserID(), date));
        }
        return allAnomalies;
    }

    @Transactional(readOnly = true)
    public List<Anomalies> getUserAnomaliesForToday(Long userId) {
        LocalDate today = LocalDate.now();
        return anomaliesRepository.findByUserUserIDAndDateAnomalie(userId, today);
    }

}
