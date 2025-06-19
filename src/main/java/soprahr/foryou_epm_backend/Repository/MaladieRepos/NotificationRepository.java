package soprahr.foryou_epm_backend.Repository.MaladieRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import soprahr.foryou_epm_backend.Model.maladie.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}