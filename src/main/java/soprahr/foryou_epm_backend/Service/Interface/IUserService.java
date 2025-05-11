package soprahr.foryou_epm_backend.Service.Interface;

import soprahr.foryou_epm_backend.Model.User;

import java.util.List;

public interface IUserService {

    List<User> retrieveAllUsers();
    User addUser(User r);
    User updateUser(User r);
    User retrieveUser(Long idRec);
    void removeUser(Long idRec);
    public User findUserByIdentifiantAndPassword(String identifiant, String password);
}
