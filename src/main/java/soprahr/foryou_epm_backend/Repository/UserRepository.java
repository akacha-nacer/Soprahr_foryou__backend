package soprahr.foryou_epm_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soprahr.foryou_epm_backend.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User  findByEmail(String email);
    User findByIdentifiantAndPassword(String identifiant, String password);
}
