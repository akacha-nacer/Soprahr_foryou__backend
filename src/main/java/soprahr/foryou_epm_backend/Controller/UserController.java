package soprahr.foryou_epm_backend.Controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import soprahr.foryou_epm_backend.Model.DTO.LoginRequestDTO;
import soprahr.foryou_epm_backend.Model.DTO.LoginResponseDTO;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Service.Interface.IUserService;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/user")
public class UserController {


    IUserService userService;

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


    @PostMapping("/add-User")
    @ResponseBody
    public User addUser(@RequestBody User r) {
        User User= userService.addUser(r);
        return User;
    }


    @PutMapping("/update-User")
    @ResponseBody
    public User updateUser(@RequestBody User r) {
        User User= userService.updateUser(r);
        return User;
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
        LoginResponseDTO response = new LoginResponseDTO(
                user.getUserID(),
                user.getFirstname(),
                user.getLastname(),
                user.getIdentifiant(),
                user.getEmail(),
                user.getRole(),
                "Login successful"
        );
        return ResponseEntity.ok(response);
    }
}
