package eg.edu.alexu.cse.mail_server.Service.command;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
@RequiredArgsConstructor
public class SendCommand implements MailCommand {
    private final UserRepository userRepository;
    private final MailRepository mailRepository;

    @Override
    @Transactional   // if error in receiver copy consider not sent copy (all or nothing)
    public void execute(ComposeEmailDTO dto) {
        if (userRepository.findByEmail(dto.getSender()).isEmpty()) {
            throw new RuntimeException("Sender email not found: " + dto.getSender());
        }

        Mail sentCopy = Mail.builder()
                .sender(dto.getSender())
                .receiver(String.join(",", dto.getReceivers()))
                .body(dto.getBody())
                .subject(dto.getSubject())
                .priority(dto.getPriority())
                .attachments(null) // still need to handle
                .folderName("SENT")
                .isRead(true)
                .timestamp(LocalDateTime.now())
                .build();

        mailRepository.save(sentCopy);

        Queue<String> receiverQueue = new LinkedList<>(dto.getReceivers());
        List<String> failedReceivers = new ArrayList<>();

        while (!receiverQueue.isEmpty()) {
            String receiverEmail = receiverQueue.poll();

            if (userRepository.findByEmail(receiverEmail).isPresent()) {
                Mail inboxCopy = Mail.builder()
                        .sender(dto.getSender())
                        .receiver(receiverEmail)
                        .subject(dto.getSubject())
                        .body(dto.getBody())
                        .priority(dto.getPriority())
                        .timestamp(LocalDateTime.now())
                        .folderName("INBOX")
                        .isRead(false)
                        .build();

                mailRepository.save(inboxCopy);
            } else {
                failedReceivers.add(receiverEmail);
            }
        }
        if (!failedReceivers.isEmpty()) {
            throw new RuntimeException("the following receivers were not found: " + String.join(",", failedReceivers));
        }
    }
}
