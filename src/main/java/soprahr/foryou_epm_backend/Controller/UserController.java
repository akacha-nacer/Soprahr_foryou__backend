package soprahr.foryou_epm_backend.Controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import soprahr.foryou_epm_backend.Model.DTO.LoginRequestDTO;
import soprahr.foryou_epm_backend.Model.DTO.LoginResponseDTO;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.UserRepository;
import soprahr.foryou_epm_backend.Service.Interface.IUserService;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST,RequestMethod.PATCH, RequestMethod.OPTIONS}, allowedHeaders = "*")
@RequestMapping("/user")
public class UserController {


    IUserService userService;
    UserRepository userRepository;

    // http://localhost:8089/User/retrieve-all-Users

    @GetMapping("/retrieve-all-Users")
    @ResponseBody
    public List<User> getUsers() {
        List<User> Users = userService.retrieveAllUsers();
        return Users;
    }


    @GetMapping("/retrieve-User/{id}")
    @ResponseBody
    public User retrieveUser(@PathVariable("id") Long id) {
        return userService.retrieveUser(id);
    }



    @PostMapping(value = "/add-User", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> addUser(
            @RequestPart(name = "user", required = true) User user,
            @RequestPart(name = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            if (profilePicture != null) {
                if (profilePicture.getSize() > 1_000_000) { // 1MB limit
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 1MB");
                }
                String contentType = profilePicture.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image");
                }
            }
            User savedUser = userService.addUser(user, profilePicture);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add user: " + e.getMessage(), e);
        }
    }

    @PutMapping(value = "/update-User", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUser(
            @RequestPart(name = "user", required = true) User user,
            @RequestPart(name = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            if (profilePicture != null) {
                if (profilePicture.getSize() > 1_000_000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 1MB");
                }
                String contentType = profilePicture.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image");
                }
            }
            User updatedUser = userService.updateUser(user, profilePicture);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user: " + e.getMessage(), e);
        }
    }


    @DeleteMapping("/removeUser/{id}")
    @ResponseBody
    public void removeUser(@PathVariable("id") Long id) {
        userService.removeUser(id);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        User user = userService.findUserByIdentifiantAndPassword(loginRequest.getIdentifiant(), loginRequest.getPassword());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid identifiant or password");
        }
        LoginResponseDTO response = new LoginResponseDTO(user, "Login successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/profile-picture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProfilePicture(@PathVariable("id") Long id) {
        byte[] picture = userService.getProfilePicture(id);
        if (picture == null) {
            return ResponseEntity.noContent().build();
        }
        String base64Picture = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(picture);
        return ResponseEntity.ok(base64Picture);
    }

    @GetMapping("/getManagerInfo/{userID}")
    public ResponseEntity<LoginResponseDTO> getManagerInfo(@PathVariable("userID") Long userID) {
        Optional<User> manager = userService.getManager(userID);
        if (manager.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manager not found");
        }
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(manager.get(), "user sent successfully");
        return ResponseEntity.ok(loginResponseDTO);
    }
}
