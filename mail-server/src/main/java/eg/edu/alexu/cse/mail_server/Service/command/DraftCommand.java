package eg.edu.alexu.cse.mail_server.Service.command;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DraftCommand implements MailCommand {
    private final UserRepository userRepository;
    private final MailRepository mailRepository;


    @Override
    @Transactional
    public void execute(ComposeEmailDTO dto) {
        if (userRepository.findByEmail(dto.getSender()).isEmpty()) {
            throw new RuntimeException("Sender email not found: " + dto.getSender());
        }
        Mail draft = Mail.builder()
                .sender(dto.getSender())
                .receiver(String.join(", ", dto.getReceivers()))
                .subject(dto.getSubject())
                .body(dto.getBody())
                .timestamp(LocalDateTime.now())
                .folderName("DRAFTS")
                .isRead(true)
                .build();

        mailRepository.save(draft);
    }
}
