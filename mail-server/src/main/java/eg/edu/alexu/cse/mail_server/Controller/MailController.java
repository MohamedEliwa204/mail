package eg.edu.alexu.cse.mail_server.Controller;

import eg.edu.alexu.cse.mail_server.Service.MailService;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @PostMapping("/send")
    public Map<String, String> sendMail(@RequestBody ComposeEmailDTO composeEmailDTO) {
        mailService.send(composeEmailDTO);
        return Map.of("message", "Email sent successfully");

    }

    @PostMapping("/draft")
    public Map<String, String> draftEmail(@RequestBody ComposeEmailDTO composeEmailDTO) {
        mailService.draft(composeEmailDTO);
        return Map.of("message", "Email drafted successfully");
    }

    /**
     * Get mail with all attachments including file data
     *
     * @param mailId the ID of the mail
     * @return EmailViewDto with attachments containing Base64-encoded file data
     */
    @GetMapping("/{mailId}")
    public ResponseEntity<EmailViewDto> getMail(@PathVariable Long mailId) throws IOException {
        EmailViewDto mail = mailService.getMailWithAttachments(mailId);
        return ResponseEntity.ok(mail);
    }


}
