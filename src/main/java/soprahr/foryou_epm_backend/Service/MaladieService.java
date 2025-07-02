package soprahr.foryou_epm_backend.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soprahr.foryou_epm_backend.Model.DTO.AbsenceDeclarationDTO;
import soprahr.foryou_epm_backend.Model.DTO.NotificationDTO;
import soprahr.foryou_epm_backend.Model.Team;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Model.maladie.AbsenceDeclaration;
import soprahr.foryou_epm_backend.Model.maladie.Justification;
import soprahr.foryou_epm_backend.Model.maladie.Notification;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.AbsenceDeclarationRepository;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.JustificationRepository;
import soprahr.foryou_epm_backend.Repository.MaladieRepos.NotificationRepository;
import soprahr.foryou_epm_backend.Repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaladieService {

    private final NotificationRepository notificationRepository;
    private final AbsenceDeclarationRepository absenceDeclarationRepository;
    private final JustificationRepository justificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification saveNotification(Notification notification, Long employeeId) {
        // Check if an active notification exists
        Optional<Notification> existingNotification = notificationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);
        if (existingNotification.isPresent()) {
            throw new IllegalStateException("An active notification already exists. Please close it before creating a new one.");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        Team team = employee.getTeam();
        if (team == null) {
            throw new IllegalArgumentException("Employee is not part of any team");
        }
        User manager = team.getManager();
        notification.setEmployee(employee);
        notification.setRecipient(manager);
        notification.setCloturee(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public AbsenceDeclaration saveAbsenceDeclaration(AbsenceDeclaration absenceDeclaration, Long employeeId, Long notificationId) {
        // Check if an active absence declaration exists
        Optional<AbsenceDeclaration> existingAbsence = absenceDeclarationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);
        if (existingAbsence.isPresent()) {
            throw new IllegalStateException("An active absence declaration already exists. Please close it before creating a new one.");
        }

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getEmployee().getUserID().equals(employeeId)) {
            throw new IllegalArgumentException("Notification does not belong to this employee");
        }
        if (notification.isCloturee()) {
            throw new IllegalArgumentException("Notification is already closed");
        }
        absenceDeclaration.setEmployee(employee);
        absenceDeclaration.setNotification(notification);
        absenceDeclaration.setCloturee(false);
        return absenceDeclarationRepository.save(absenceDeclaration);
    }

    @Transactional
    public Justification saveJustification(Justification justification, Long absenceDeclarationId) {
        AbsenceDeclaration absenceDeclaration = absenceDeclarationRepository.findById(absenceDeclarationId)
                .orElseThrow(() -> new IllegalArgumentException("Absence declaration not found"));
        if (absenceDeclaration.isCloturee()) {
            throw new IllegalArgumentException("Absence declaration is already closed");
        }
        justification.setAbsenceDeclaration(absenceDeclaration);
        return justificationRepository.save(justification);
    }

    @Transactional
    public void closeSickLeave(Long employeeId) {
        Optional<Notification> notification = notificationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);
        Optional<AbsenceDeclaration> absenceDeclaration = absenceDeclarationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);

        if (notification.isPresent()) {
            Notification n = notification.get();
            n.setCloturee(true);
            notificationRepository.save(n);
        }

        if (absenceDeclaration.isPresent()) {
            AbsenceDeclaration ad = absenceDeclaration.get();
            ad.setCloturee(true);
            absenceDeclarationRepository.save(ad);
        }

        if (!notification.isPresent() && !absenceDeclaration.isPresent()) {
            throw new IllegalStateException("No active sick leave process to close");
        }
    }

    public Optional<Notification> getActiveNotification(Long employeeId) {
        return notificationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);
    }

    public Optional<AbsenceDeclaration> getActiveDeclaration(Long employeeId) {
        return absenceDeclarationRepository.findByEmployeeUserIDAndClotureeFalse(employeeId);
    }

    public List<Notification> getNotificationsForManager(Long managerId) {
        return notificationRepository.findByRecipientUserIDAndClotureeFalse(managerId);
    }

    public Boolean checkIfLateness(Long id){
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.isPresent() && notification.get().isRetard();
    }


    public List<NotificationDTO> getNotificationsWithDeclarationsForManager(Long managerId) {

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));

        List<Notification> notifications = notificationRepository.findByRecipientUserIDAndClotureeFalse(managerId);

        return notifications.stream().map(notification -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setMessage(notification.getMessage());
            dto.setRetard(notification.isRetard());
            dto.setValidated(notification.isValidated());
            dto.setCloturee(notification.isCloturee());
            dto.setCreatedAt(notification.getCreatedAt());
            dto.setEmployeeId(notification.getEmployee().getUserID());
            dto.setEmployeeName(notification.getEmployee().getFirstname() + " " + notification.getEmployee().getLastname());

            List<AbsenceDeclaration> declarations = absenceDeclarationRepository.findByNotificationId(notification.getId());
            List<AbsenceDeclarationDTO> declarationDTOs = declarations.stream().map(declaration -> {
                AbsenceDeclarationDTO declDTO = new AbsenceDeclarationDTO();
                declDTO.setId(declaration.getId());
                declDTO.setProlongation(declaration.isProlongation());
                declDTO.setDateDebut(declaration.getDateDebut());
                declDTO.setDateFin(declaration.getDateFin());
                declDTO.setCloturee(declaration.isCloturee());
                return declDTO;
            }).collect(Collectors.toList());
            dto.setAbsenceDeclarations(declarationDTOs);

            return dto;
        }).collect(Collectors.toList());
    }
}