package soprahr.foryou_epm_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soprahr.foryou_epm_backend.Model.Team;
import soprahr.foryou_epm_backend.Model.User;
import soprahr.foryou_epm_backend.Repository.TeamRepository;
import soprahr.foryou_epm_backend.Repository.UserRepository;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Team> addTeam(@RequestBody Team team, @RequestParam("managerId") Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        team.setManager(manager);
        teamRepository.save(team);
        return ResponseEntity.ok(teamRepository.save(team));
    }

    @PostMapping("/addMember")
    public ResponseEntity<String> addMember(@RequestParam("teamId") Long teamId, @RequestParam("userId") Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTeam(team);
        userRepository.save(user);
        return ResponseEntity.ok("Member added to team");
    }
}
