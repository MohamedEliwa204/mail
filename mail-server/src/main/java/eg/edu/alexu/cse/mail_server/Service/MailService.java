package eg.edu.alexu.cse.mail_server.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import eg.edu.alexu.cse.mail_server.Service.command.DraftCommand;
import eg.edu.alexu.cse.mail_server.Service.command.SendCommand;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
    private final MailRepository mailRepository;
    private final UserRepository userRepository;
    private final SendCommand sendCommand;
    private final DraftCommand draftCommand;

    public void send(ComposeEmailDTO composeEmailDTO) {
        sendCommand.execute(composeEmailDTO);
    }

    public void draft(ComposeEmailDTO composeEmailDTO) {
        draftCommand.execute(composeEmailDTO);
    }

    // Get inbox mails
    public List<Mail> getInboxMails(String userEmail) {
        return mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, "INBOX");
    }

    // Get sent mails
    public List<Mail> getSentMails(String userEmail) {
        return mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "SENT");
    }

    // Get draft mails
    public List<Mail> getDraftMails(String userEmail) {
        return mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "DRAFTS");
    }

    // Get mails by folder
    public List<Mail> getMailsByFolder(String userEmail, String folderName) {
        if ("all".equals(folderName)) {
            return mailRepository.findByReceiverOrSenderOrderByTimestampDesc(userEmail, userEmail);
        }
        return mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, folderName);
    }

    // Get mail by ID
    public Mail getMailById(Long mailId) {
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new RuntimeException("Mail not found with id: " + mailId));
    }

    // Mark as read
    public void markAsRead(Long mailId) {
        Mail mail = getMailById(mailId);
        mail.setRead(true);
        mailRepository.save(mail);
    }

    // Delete mail (soft delete - move to trash)
    public void deleteMail(Long mailId) {
        Mail mail = getMailById(mailId);
        mail.setFolderName("trash");
        mailRepository.save(mail);
    }
}
