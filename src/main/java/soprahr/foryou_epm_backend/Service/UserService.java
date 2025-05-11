package soprahr.foryou_epm_backend.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.UserRepository;
import soprahr.foryou_epm_backend.Service.Interface.IUserService;

import java.util.List;

@Service
    @Slf4j
    @AllArgsConstructor
    public class UserService implements IUserService {

        UserRepository userRepository;

        @Override
        public List<User> retrieveAllUsers() {
            return userRepository.findAll();
        }

        @Override
        public User addUser(User r) {
            return userRepository.save(r);
        }

        @Override
        public User updateUser(User r) {
            return userRepository.save(r);
        }

        @Override
        public User retrieveUser(Long id) {
            return userRepository.findById(id).orElse(null);
        }

        @Override
        public void removeUser(Long id) {
            log.debug("debugging");
            userRepository.deleteById(id);

        }

        @Override
        public User findUserByIdentifiantAndPassword(String identifiant, String password) {
            System.out.println(userRepository.findByIdentifiantAndPassword(identifiant, password));
        return userRepository.findByIdentifiantAndPassword(identifiant, password);


        }
}
