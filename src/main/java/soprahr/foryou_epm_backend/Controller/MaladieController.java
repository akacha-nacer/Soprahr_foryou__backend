package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soprahr.foryou_epm_backend.Exceptions.ResourceNotFoundException;
import soprahr.foryou_epm_backend.Model.DTO.JustificationDTO;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;
import soprahr.foryou_epm_backend.Model.maladie.Justification;
import soprahr.foryou_epm_backend.Model.maladie.Notification;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.AbsenceDeclarationRepository;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.JustificationRepository;
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
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET,RequestMethod.PUT, RequestMethod.POST,RequestMethod.PATCH, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class MaladieController {

    private final MaladieService maladieService;
    private final NotificationRepository notificationRepository ;
    private final JustificationRepository justificationRepository;
    private final AbsenceDeclarationRepository absenceDeclarationRepository;


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

        if (justificatif.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String contentType = justificatif.getContentType();
        if (!"application/pdf".equals(contentType) &&
                !"image/jpeg".equals(contentType) &&
                !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG files are allowed");
        }

        Justification justification = new Justification();
        justification.setFileContent(justificatif.getBytes());
        justification.setOriginalDepose(originalDepose);
        justification.setAccidentTravail(accidentTravail);
        if (dateAccident != null && !dateAccident.isEmpty()) {
            justification.setDateAccident(LocalDate.parse(dateAccident));
        }
        maladieService.saveJustification(justification, absenceDeclarationId);
        return ResponseEntity.ok("Justification saved successfully in database");
    }

    @GetMapping("/justify/{id}/file")
    public ResponseEntity<byte[]> getJustificationFile(@PathVariable Long id) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Justification not found"));
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=justification_" + id + ".pdf")
                .body(justification.getFileContent());
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

    @PutMapping("/declaration/{id}/validate")
    public ResponseEntity<Void> validateDeclaration(@PathVariable Long id) {
        AbsenceDeclaration declaration = absenceDeclarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        declaration.getNotification().setValidated(true);
        declaration.setValidated(true);
        absenceDeclarationRepository.save(declaration);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/notification/{id}/refuser")
    public ResponseEntity<Void> Refuser_fermer_Notif(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (notification.isRetard())
        {
            maladieService.closeSickLeave(notification.getEmployee().getUserID());
            notification.setValidated(true);
            notificationRepository.save(notification);
        } else {
            maladieService.closeSickLeave(notification.getEmployee().getUserID());
            notification.setValidated(false);
            List<AbsenceDeclaration> absenceDeclarations =absenceDeclarationRepository.findByNotificationId(notification.getId());
            if (!absenceDeclarations.isEmpty()){
                absenceDeclarations.stream().forEach(absenceDeclaration -> {
                    absenceDeclaration.setValidated(false);
                    absenceDeclarationRepository.save(absenceDeclaration);
                });
            }
            notificationRepository.save(notification);}
        return ResponseEntity.ok().build();
    }

    @GetMapping("/justifications/{id}/download")
    public ResponseEntity<byte[]> downloadJustification(@PathVariable Long id) {
        Justification justification = justificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Justification not found"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"justification_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(justification.getFileContent());
    }

    @GetMapping("/test/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Justification>> test(@PathVariable Long id){
        List<Justification> justifications = justificationRepository.findByAbsenceDeclaration_Id(id);
        return  ResponseEntity.ok(justifications);
    }
}