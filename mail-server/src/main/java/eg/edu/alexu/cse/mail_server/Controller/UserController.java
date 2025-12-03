package eg.edu.alexu.cse.mail_server.Controller;

import eg.edu.alexu.cse.mail_server.Service.UserService;
import eg.edu.alexu.cse.mail_server.dto.UserFormDto;
import eg.edu.alexu.cse.mail_server.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody UserFormDto form){
        return userService.register(form);
    }

    @PostMapping("/login")
    public UserResponseDto login(@RequestBody UserFormDto form){
        return userService.login(form);
    }
}
