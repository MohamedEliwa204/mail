package eg.edu.alexu.cse.mail_server.Controller;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Service.MailService;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @PostMapping("/send-with-attachments")
    public Map<String, String> sendMail(
            @RequestPart("email") ComposeEmailDTO composeEmailDTO,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) throws IOException {
        mailService.sendWithAttachments(composeEmailDTO, attachments);
        return Map.of("message", "Email sent successfully");
    }

    @PostMapping("/draft")
    public Map<String, String> draftEmail(@RequestBody ComposeEmailDTO composeEmailDTO) {
        mailService.draft(composeEmailDTO);
        return Map.of("message", "Email drafted successfully");
    }

    // Get inbox emails
    @GetMapping("/inbox/{userEmail}")
    public List<EmailViewDto> getInboxMails(@PathVariable String userEmail) {
        return mailService.getInboxMails(userEmail);
    }

    // Get sent emails
    @GetMapping("/sent/{userEmail}")
    public List<EmailViewDto> getSentMails(@PathVariable String userEmail) {
        return mailService.getSentMails(userEmail);
    }
    
    // Get draft emails
    @GetMapping("/drafts/{userEmail}")
    public List<EmailViewDto> getDraftMails(@PathVariable String userEmail) {
        return mailService.getDraftMails(userEmail);
    }

    // Get trash emails
    @GetMapping("/trash/{userEmail}")
    public List<EmailViewDto> getTrashMails(@PathVariable String userEmail) {
        return mailService.getTrashMails(userEmail);
    }

    // Get emails by folder
    @GetMapping("/folder/{userEmail}/{folderName}")
    public List<EmailViewDto> getMailsByFolder(@PathVariable String userEmail, @PathVariable String folderName) {
        return mailService.getMailsByFolder(userEmail, folderName);
    }

    // Get mail by ID
    @GetMapping("/{mailId}")
    public Mail getMailById(@PathVariable Long mailId) {
        return mailService.getMailById(mailId);
    }

    // Mark as read
    @PutMapping("/{mailId}/read")
    public Map<String, String> markAsRead(@PathVariable Long mailId) {
        mailService.markAsRead(mailId);
        return Map.of("message", "Mail marked as read");
    }

    // Delete mail (move to trash)
    @DeleteMapping("/{mailId}")
    public Map<String, String> deleteMail(@PathVariable Long mailId) {
        mailService.deleteMail(mailId);
        return Map.of("message", "Mail deleted successfully");
    }

}
