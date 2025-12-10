package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Service.command.DraftCommand;
import eg.edu.alexu.cse.mail_server.Service.command.GetMailCommand;
import eg.edu.alexu.cse.mail_server.Service.command.SendCommand;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailService {
    private final SendCommand sendCommand;
    private final DraftCommand draftCommand;
    private final GetMailCommand getMailCommand;

    public void send(ComposeEmailDTO composeEmailDTO) {
        sendCommand.execute(composeEmailDTO);
    }

    public void draft(ComposeEmailDTO composeEmailDTO) {
        draftCommand.execute(composeEmailDTO);
    }

    // Get inbox mails
    public List<Mail> getInboxMails(String userEmail) {
        return mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, "inbox");
    }

    // Get sent mails
    public List<Mail> getSentMails(String userEmail) {
        return mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "sent");
    }

    // Get draft mails
    public List<Mail> getDraftMails(String userEmail) {
        return mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "drafts");
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
    /**
     * Get mail with all attachments including file data
     *
     * @param mailId the ID of the mail
     * @return EmailViewDto with attachments containing file data as Base64
     * @throws IOException if file reading fails
     */
    public EmailViewDto getMailWithAttachments(Long mailId) throws IOException {
        return getMailCommand.execute(mailId);
    }
}
