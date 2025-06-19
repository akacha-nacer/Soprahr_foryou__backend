package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;
import soprahr.foryou_epm_backend.Model.maladie.Justification;
import soprahr.foryou_epm_backend.Model.maladie.Notification;
import soprahr.foryou_epm_backend.Service.MaladieService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/maladie")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MaladieController {

    private final MaladieService maladieService;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/notify")
    public ResponseEntity<String> saveNotification(@RequestBody Notification notification) {
        maladieService.saveNotification(notification);
        return ResponseEntity.ok("Notification saved successfully");
    }

    @PostMapping("/declare")
    public ResponseEntity<String> saveAbsenceDeclaration(@RequestBody AbsenceDeclaration absenceDeclaration) {
        maladieService.saveAbsenceDeclaration(absenceDeclaration);
        return ResponseEntity.ok("Absence declaration saved successfully");
    }

    @PostMapping("/justify")
    public ResponseEntity<String> saveJustification(
            @RequestParam("justificatif") MultipartFile justificatif,
            @RequestParam("originalDepose") boolean originalDepose,
            @RequestParam("accidentTravail") boolean accidentTravail,
            @RequestParam(value = "dateAccident", required = false) String dateAccident) throws IOException {


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


        maladieService.saveJustification(justification);


        return ResponseEntity.ok("Justification saved successfully. File: " + fileName);
    }
}