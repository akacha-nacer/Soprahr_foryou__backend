package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soprahr.foryou_epm_backend.Exceptions.ResourceNotFoundException;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;
import soprahr.foryou_epm_backend.Model.maladie.Justification;
import soprahr.foryou_epm_backend.Model.maladie.Notification;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.NotificationRepository;
import soprahr.foryou_epm_backend.Service.MaladieService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/maladie")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST,RequestMethod.PATCH, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class MaladieController {

    private final MaladieService maladieService;
    private final NotificationRepository notificationRepository ;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/notify")
    public ResponseEntity<Notification> saveNotification(
            @RequestBody Notification notification,
            @RequestParam("employeeId") Long employeeId) {
        Notification savedNotification = maladieService.saveNotification(notification, employeeId);
        return ResponseEntity.ok(savedNotification);
    }

    @PostMapping("/declare")
    public ResponseEntity<AbsenceDeclaration> saveAbsenceDeclaration(
            @RequestBody AbsenceDeclaration absenceDeclaration,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam(value = "notificationId", required = false) Long notificationId) {
        if (notificationId == null) {
            Optional<Notification> activeNotification = maladieService.getActiveNotification(employeeId);
            if (activeNotification.isEmpty()) {
                throw new IllegalStateException("No active notification found. Please create one first.");
            }
            notificationId = activeNotification.get().getId();
        }
        AbsenceDeclaration savedAbsence = maladieService.saveAbsenceDeclaration(absenceDeclaration, employeeId, notificationId);
        return ResponseEntity.ok(savedAbsence);
    }

    @PostMapping("/justify")
    public ResponseEntity<String> saveJustification(
            @RequestParam("justificatif") MultipartFile justificatif,
            @RequestParam("originalDepose") boolean originalDepose,
            @RequestParam("accidentTravail") boolean accidentTravail,
            @RequestParam(value = "dateAccident", required = false) String dateAccident,
            @RequestParam("absenceDeclarationId") Long absenceDeclarationId) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = System.currentTimeMillis() + "_" + justificatif.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        justificatif.transferTo(filePath.toFile());
        Justification justification = new Justification();
        justification.setJustificatifFileName(fileName);
        justification.setOriginalDepose(originalDepose);
        justification.setAccidentTravail(accidentTravail);
        if (dateAccident != null && !dateAccident.isEmpty()) {
            justification.setDateAccident(LocalDate.parse(dateAccident));
        }
        maladieService.saveJustification(justification, absenceDeclarationId);
        return ResponseEntity.ok("Justification saved successfully. File: " + fileName);
    }

    @PostMapping("/close")
    public ResponseEntity<String> closeSickLeave(@RequestParam("employeeId") Long employeeId) {
        maladieService.closeSickLeave(employeeId);
        return ResponseEntity.ok("Sick leave process closed successfully");
    }

    @GetMapping("/notifications/active")
    public ResponseEntity<Optional<Notification>> getActiveNotification(@RequestParam("employeeId") Long employeeId) {
        return ResponseEntity.ok(maladieService.getActiveNotification(employeeId));
    }

    @GetMapping("/declaration/active")
    public ResponseEntity<Optional<AbsenceDeclaration>> getActiveDeclaration(@RequestParam("employeeId") Long employeeId) {
        return ResponseEntity.ok(maladieService.getActiveDeclaration(employeeId));
    }

    @GetMapping("/notifications/manager")
    public ResponseEntity<List<Notification>> getNotificationsForManager(@RequestParam("managerId") Long managerId) {
        return ResponseEntity.ok(maladieService.getNotificationsForManager(managerId));
    }


    @GetMapping("/notifications/retard/{userID}")
    public Boolean checkIfLateness(@PathVariable Long userID) {
        return maladieService.checkIfLateness(userID);
    }

    @GetMapping("/manager/{managerId}/getnotif")
    public ResponseEntity<List<NotificationDTO>> getNotificationsWithDeclarations(@PathVariable Long managerId) {
        List<NotificationDTO> notifications = maladieService.getNotificationsWithDeclarationsForManager(managerId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/notifications/{id}/validate")
    public ResponseEntity<Void> validateNotification(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setValidated(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }
}