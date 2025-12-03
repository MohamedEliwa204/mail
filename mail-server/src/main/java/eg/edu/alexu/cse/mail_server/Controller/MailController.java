package eg.edu.alexu.cse.mail_server.Controller;

import eg.edu.alexu.cse.mail_server.Service.MailService;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @RequestMapping("/send")
    public Map<String, String> sendMail(@RequestBody ComposeEmailDTO composeEmailDTO) {
        mailService.send(composeEmailDTO);
        return Map.of("message", "Email sent successfully");

    }

    @RequestMapping("/draft")
    public Map<String, String> draftEmail(@RequestBody ComposeEmailDTO composeEmailDTO) {
        mailService.draft(composeEmailDTO);
        return Map.of("message", "Email drafted successfully");
    }


}
