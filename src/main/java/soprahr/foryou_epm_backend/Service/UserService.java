package soprahr.foryou_epm_backend.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soprahr.foryou_epm_backend.Model.DTO.LoginResponseDTO;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.UserRepository;
import soprahr.foryou_epm_backend.Service.Interface.IUserService;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
    public User addUser(User user, MultipartFile profilePicture) {
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                user.setProfilePicture(profilePicture.getBytes()); // Store as binary
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile picture", e);
            }
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user, MultipartFile profilePicture) {
        Optional<User> existingUserOptional = userRepository.findById(user.getUserID());
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            existingUser.setFirstname(user.getFirstname());
            existingUser.setLastname(user.getLastname());
            existingUser.setIdentifiant(user.getIdentifiant());
            existingUser.setEmail(user.getEmail());
            existingUser.setPoste(user.getPoste());
            existingUser.setPassword(user.getPassword());
            existingUser.setRole(user.getRole());
            existingUser.setTeam(user.getTeam());
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    existingUser.setProfilePicture(profilePicture.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process profile picture", e);
                }
            }
            return userRepository.save(existingUser);
        }
        throw new RuntimeException("User not found");
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
        @Transactional(readOnly = true)
        public User findUserByIdentifiantAndPassword(String identifiant, String password) {
            System.out.println(userRepository.findByIdentifiantAndPassword(identifiant, password));
        return userRepository.findByIdentifiantAndPassword(identifiant, password);


        }

    @Override
    @Transactional(readOnly = true)
    public byte[] getProfilePicture(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfilePicture)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getManager(Long userID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isEmpty() || user.get().getTeam() == null || user.get().getTeam().getManager() == null) {
            return Optional.empty();
        }
        Long managerID = user.get().getTeam().getManager().getUserID();
        return userRepository.findById(managerID);
    }

}
