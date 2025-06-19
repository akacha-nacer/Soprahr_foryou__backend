package soprahr.foryou_epm_backend.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;
import soprahr.foryou_epm_backend.Model.maladie.Justification;
import soprahr.foryou_epm_backend.Model.maladie.Notification;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.AbsenceDeclarationRepository;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.JustificationRepository;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.NotificationRepository;

@Service
@RequiredArgsConstructor
public class MaladieService {

    private final NotificationRepository notificationRepository;
    private final AbsenceDeclarationRepository absenceDeclarationRepository;
    private final JustificationRepository justificationRepository;

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public AbsenceDeclaration saveAbsenceDeclaration(AbsenceDeclaration absenceDeclaration) {
        return absenceDeclarationRepository.save(absenceDeclaration);
    }

    public Justification saveJustification(Justification justification) {
        return justificationRepository.save(justification);
    }
}