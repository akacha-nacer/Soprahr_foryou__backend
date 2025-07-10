package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soprahr.foryou_epm_backend.Model.Embauche.DepartementNaiss;
import soprahr.foryou_epm_backend.Model.Journee.Anomalies;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeure;
import soprahr.foryou_epm_backend.Model.Journee.NatureHeureModificationRequest;
import soprahr.foryou_epm_backend.Model.Journee.Pointage;
import soprahr.foryou_epm_backend.Model.Role;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureModificationRequestRepository;
import soprahr.foryou_epm_backend.Repository.JourneeRepos.NatureHeureRepository;
import soprahr.foryou_epm_backend.Repository.UserRepository;
import soprahr.foryou_epm_backend.Service.JourneeService;

import java.util.List;

@RestController
@RequestMapping("/api/journee")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST,RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.PUT}, allowedHeaders = "*")
public class JourneeController {

    @Autowired
    private JourneeService journeeService;
    @Autowired
    private UserRepository userRepository;
    private final NatureHeureRepository natureHeureRepository;
    private final NatureHeureModificationRequestRepository modificationRequestRepo;


    @PostMapping("/save_anomalie")
    public ResponseEntity<String> saveAnomalie(@RequestBody Anomalies anomalies, @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        anomalies.setUser(user);
        journeeService.saveAnomalie(anomalies);
        return ResponseEntity.ok("anomalie saved successfully");
    }

    @PostMapping("/save_nature_heure")
    public ResponseEntity<String> saveNHeure(@RequestBody NatureHeure natureHeure, @RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        natureHeure.setUser(user);
        journeeService.saveNatureHeure(natureHeure);
        return ResponseEntity.ok("nature Heure saved successfully");
    }

    @GetMapping("/retrieve-all-NatureHrs")
    @ResponseBody
    public List<NatureHeure> getNatureHeures(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        List<NatureHeure> natureHeures = journeeService.getAllUserNatureHeures(userId);
        return natureHeures;
    }

    @GetMapping("/retrieve-all-Anomalies")
    @ResponseBody
    public List<Anomalies> getAnomalies(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        List<Anomalies> anomalies = journeeService.getAllUserAnomalies(userId);
        return anomalies;
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
            request.setNewCommentaire(updatedNatureHeure.getCommentaire());

            NatureHeure original = natureHeureRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("NatureHeure not found"));
            request.setOriginalNatureHeure(original);
            request.setRequestedBy(user);
            request.setApproved(false);
            request.setRejected(false);
            modificationRequestRepo.save(request);

            return ResponseEntity.ok("Modification request submitted for employee.");
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
        modificationRequestRepo.save(request);

        return ResponseEntity.ok("Modification request submitted.");
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

        return ResponseEntity.ok("Request approved and NatureHeure updated successfully.");
    }
}
