package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soprahr.foryou_epm_backend.Model.DTO.NatureHeureDTO;
import soprahr.foryou_epm_backend.Model.DTO.NatureHeureDeleteDTO;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.Embauche.DepartementNaiss;
import soprahr.foryou_epm_backend.Model.Journee.*;
import soprahr.foryou_epm_backend.Model.Role;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureDeletionRequestRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureModificationRequestRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureRequestRepository;
import soprahr.foryou_epm_backend.Repository.UserRepository;
import soprahr.foryou_epm_backend.Service.JourneeService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journee")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET,RequestMethod.DELETE, RequestMethod.POST,RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.PUT}, allowedHeaders = "*")
public class JourneeController {

    @Autowired
    private JourneeService journeeService;
    @Autowired
    private UserRepository userRepository;
    private final NatureHeureRepository natureHeureRepository;
    private final NatureHeureModificationRequestRepository modificationRequestRepo;
    private final NatureHeureDeletionRequestRepository deletionRequestRepo;
    private final NatureHeureRequestRepository natureHeureRequestRepository ;


    @PostMapping("/save_anomalie")
    public ResponseEntity<String> saveAnomalie(@RequestBody Anomalies anomalies, @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        anomalies.setUser(user);
        journeeService.saveAnomalie(anomalies);
        return ResponseEntity.ok("anomalie saved successfully");
    }

    @PostMapping("/save_nature_heure")
    public ResponseEntity<NatureHeureRequest> saveNatureHeure(@RequestBody NatureHeure natureHeure, @RequestParam Long userId) {
        return ResponseEntity.ok(journeeService.saveNatureHeure(natureHeure, userId));
    }

    @PostMapping("/approve_nature_heure_request/{requestId}")
    public ResponseEntity<NatureHeure> approveNatureHeureRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        return ResponseEntity.ok(journeeService.approveNatureHeureRequest(requestId, managerId));
    }

    @PostMapping("/reject_nature_heure_request/{requestId}")
    public ResponseEntity<Void> rejectNatureHeureRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        journeeService.rejectNatureHeureRequest(requestId, managerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve_modification_request/{requestId}")
    public ResponseEntity<Void> approveNatureHeureModifRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        journeeService.approveNatureHeureModifRequest(requestId, managerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject_modification_request/{requestId}")
    public ResponseEntity<Void> rejectNatureHeureModifRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        journeeService.rejectNatureHeureModifRequest(requestId, managerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending_requests")
    public ResponseEntity<List<NatureHeureRequest>> getPendingRequests(@RequestParam Long managerId) {
        return ResponseEntity.ok(journeeService.getPendingRequests(managerId));
    }

    @GetMapping("/retrieve-all-NatureHrs")
    @ResponseBody
    public List<NatureHeure> getNatureHeures(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        List<NatureHeure> natureHeures = journeeService.getAllUserNatureHeures(userId);
        return natureHeures;
    }

    @GetMapping("/retrieve-all-NatureHrsRequests")
    @ResponseBody
    public List<NatureHeureRequest> getNatureHeureRequests(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return natureHeureRequestRepository.findAllByUserUserID(userId);
    }

    @GetMapping("/retrieve-all-NatureHrsModifRequests")
    @ResponseBody
    public List<NatureHeureModificationRequest> getNatureHeureModifRequests(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return modificationRequestRepo.findAllByRequestedById(userId);
    }

    @GetMapping("/retrieve-all-NatureHrsDelfRequests")
    @ResponseBody
    public List<NatureHeureDeletionRequest> getNatureHeureDelRequests(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        return deletionRequestRepo.findAllByRequestedBy_UserID(userId);
    }

    @GetMapping("/retrieve-all-Anomalies")
    @ResponseBody
    public List<Anomalies> getAnomalies(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        List<Anomalies> anomalies = journeeService.getAllUserAnomalies(userId);
        return anomalies;
    }

    @GetMapping("/pending_modification_requests")
    public ResponseEntity<List<NatureHeureDTO>> getPendingModificationRequests(@RequestParam Long managerId) {
        return ResponseEntity.ok(journeeService.getPendingModificationRequests(managerId));
    }

    @GetMapping("/retrieve-all-Pointages")
    @ResponseBody
    public List<Pointage> getPointages(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        List<Pointage> pointages = journeeService.getAllPointages(userId);
        return pointages;
    }

    @PostMapping("/save_pointages")
    public ResponseEntity<String> savePointages(@RequestBody List<Pointage> pointages, @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        for (Pointage pointage :pointages){
            pointage.setUser(user);
        }
        journeeService.savePointages(pointages);
        return ResponseEntity.ok("pointages saved successfully");
    }

    @PutMapping("/update_nature_heure/{id}")
    public ResponseEntity<?> updateNatureHeure(
            @PathVariable Long id,
            @RequestBody NatureHeure updatedNatureHeure,
            @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.MANAGER) {
            NatureHeure updated = journeeService.updateNatureHeure(id, updatedNatureHeure);
            return ResponseEntity.ok(updated);
        } else {
            NatureHeureModificationRequest request = new NatureHeureModificationRequest();
            request.setNewNatureHeure(updatedNatureHeure.getNature_heure());
            request.setNewHeureDebut(updatedNatureHeure.getHeureDebut());
            request.setNewHeureFin(updatedNatureHeure.getHeureFin());
            request.setNewDuree(updatedNatureHeure.getDuree());
            request.setRequestedById(userId);
            request.setNewCommentaire(updatedNatureHeure.getCommentaire());

            NatureHeure original = natureHeureRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found"));
            request.setOriginalNatureHeure(original);
            request.setRequestedBy(user);
            request.setApproved(false);
            request.setRejected(false);
            modificationRequestRepo.save(request);

            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/request_update_nature_heure")
    public ResponseEntity<String> requestNatureHeureUpdate(
            @RequestBody NatureHeureModificationRequest request,
            @RequestParam("userId") Long userId,
            @RequestParam("natureHeureId") Long natureHeureId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NatureHeure original = natureHeureRepository.findById(natureHeureId)
                .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found"));

        // Store the exact values provided in the request (no fallback to original values)
        request.setOriginalNatureHeure(original);

        request.setRequestedBy(user);
        request.setApproved(false);
        request.setRejected(false);
        request.setRequestedById(user.getUserID());
        modificationRequestRepo.save(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve_request/{requestId}")
    public ResponseEntity<String> approveRequest(@PathVariable Long requestId) {
        NatureHeureModificationRequest request = modificationRequestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Retrieve the original NatureHeure
        NatureHeure nh = request.getOriginalNatureHeure();
        if (nh == null) {
            throw new IllegalArgumentException("Original NatureHeure not found for request ID: " + requestId);
        }

        // Update only the fields provided in the request (non-null values)
        if (request.getNewNatureHeure() != null) {
            nh.setNature_heure(request.getNewNatureHeure());
        }
        if (request.getNewHeureDebut() != null) {
            nh.setHeureDebut(request.getNewHeureDebut());
        }
        if (request.getNewHeureFin() != null) {
            nh.setHeureFin(request.getNewHeureFin());
        }
        if (request.getNewDuree() != null) {
            nh.setDuree(request.getNewDuree());
        }

        if (request.getNewCommentaire() != null) {
            nh.setCommentaire(request.getNewCommentaire());
        }

        // Update status and save
        nh.setIsValidee(true);
        natureHeureRepository.save(nh);
        request.setApproved(true);
        request.setRejected(false);
        modificationRequestRepo.save(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/request_delete_nature_heure/{natureHeureId}")
    public ResponseEntity<Map<String, String>> requestNatureHeureDeletion(
            @PathVariable Long natureHeureId,
            @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NatureHeure original = natureHeureRepository.findById(natureHeureId)
                .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found"));

        NatureHeureDeletionRequest request = new NatureHeureDeletionRequest();
        request.setOriginalNatureHeure(original);
        request.setRequestedBy(user);
        request.setApproved(false);
        request.setRejected(false);
        deletionRequestRepo.save(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Deletion request submitted.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve_deletion_request/{requestId}")
    public ResponseEntity<String> approveDeletionRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        journeeService.approveDeletionRequest(requestId, managerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject_deletion_request/{requestId}")
    public ResponseEntity<String> rejectDeletionRequest(@PathVariable Long requestId, @RequestParam Long managerId) {
        journeeService.rejectDeletionRequest(requestId, managerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending_deletion_requests")
    public ResponseEntity<List<NatureHeureDeleteDTO>> getPendingDeletionRequests(@RequestParam Long managerId) {
        return ResponseEntity.ok(journeeService.getPendingDeletionRequests(managerId));
    }

    @DeleteMapping("/delete_nature_heure/{id}")
    public ResponseEntity<String> deleteNatureHeure(@PathVariable Long id, @RequestParam Long userId) {
        journeeService.deleteNatureHeure(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nature_heure_notifications")
    public ResponseEntity<List<NotificationDTO>> getNatureHeureNotifications(@RequestParam Long managerId) {
        return ResponseEntity.ok(journeeService.getNatureHeureNotifications(managerId));
    }


    @GetMapping("/anomalies/today")
    public ResponseEntity<List<Anomalies>> getUserAnomaliesForToday(@RequestParam Long userId) {
        try {
            List<Anomalies> anomalies = journeeService.getUserAnomaliesForToday(userId);
            return ResponseEntity.ok(anomalies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/anomalies/generate/{userId}")
    public ResponseEntity<List<Anomalies>> generateAnomaliesForUser(
            @PathVariable Long userId,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Anomalies> anomalies = journeeService.generateAnomaliesForUser(userId, localDate);
            return ResponseEntity.ok(anomalies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
