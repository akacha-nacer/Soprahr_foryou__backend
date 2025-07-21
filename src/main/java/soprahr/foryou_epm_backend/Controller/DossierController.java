package soprahr.foryou_epm_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soprahr.foryou_epm_backend.Model.DTO.DossierDTO;
import soprahr.foryou_epm_backend.Model.Embauche.DepartementNaiss;
import soprahr.foryou_epm_backend.Service.DossierService;

import java.util.List;

@RestController
@RequestMapping("/4you/embauche_detaillée")
@CrossOrigin(origins = "http://localhost:4200")
public class DossierController {
    @Autowired
    private DossierService dossierService;

    @PostMapping("/save_emb")
    public ResponseEntity<String> saveDossier(@RequestBody DossierDTO dossierDTO) {
        dossierService.saveDossier(dossierDTO);
        return ResponseEntity.ok("File saved successfully");
    }

    @GetMapping("/get_dossiers")
    public ResponseEntity<List<DossierDTO>> getAllDossiers() {
        List<DossierDTO> dossiers = dossierService.getAllDossier();
        return ResponseEntity.ok(dossiers);
    }

    @PostMapping("/save_dep")
    public ResponseEntity<String> saveDepartementNaiss(@RequestBody List<DepartementNaiss> dep) {
        dossierService.saveDepartementNaiss(dep);
        return ResponseEntity.ok("departement saved successfully");
    }

    @GetMapping("/get_dep")
    public List<DepartementNaiss> RetrieveDepartementNaiss() {
        List<DepartementNaiss> departementNaiss = dossierService.RetrieveDepartementNaiss();
        return departementNaiss;
    }
}
