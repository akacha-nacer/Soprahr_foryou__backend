package soprahr.foryou_epm_backend.Service.Interface;

import org.springframework.web.multipart.MultipartFile;
import soprahr.foryou_epm_backend.Model.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    List<User> retrieveAllUsers();
    User addUser(User user, MultipartFile profilePicture);
    User updateUser(User user, MultipartFile profilePicture);
    User retrieveUser(Long idRec);
    void removeUser(Long idRec);
    public User findUserByIdentifiantAndPassword(String identifiant, String password);
    byte[] getProfilePicture(Long userId);
    public Optional<User> getManager(Long userID);


}
