package soprahr.foryou_epm_backend.Repository.MaladieRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.maladie.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByEmployeeUserIDAndClotureeFalse(Long employeeId);
    List<Notification> findByRecipientUserID(Long recipientId);
}