// ...existing code...
import org.ovss.onlinevirtualsimulationsystem.dto.DTO_LoginResponse;
import org.ovss.onlinevirtualsimulationsystem.dto.DTO_User;
import org.ovss.onlinevirtualsimulationsystem.service.SRV_User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// ...existing code...
@RestController
@RequestMapping("/api/users")
public class CTRL_User {

    @Autowired
    private SRV_User userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DTO_User loginRequest) {
        String jwt = userService.login(loginRequest.getUserName(), loginRequest.getPassword());
        return ResponseEntity.ok(new DTO_LoginResponse(jwt));
    }
}

