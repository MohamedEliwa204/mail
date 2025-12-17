package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Service.command.DraftCommand;
import eg.edu.alexu.cse.mail_server.Service.command.GetMailCommand;
import eg.edu.alexu.cse.mail_server.Service.command.SendCommand;
import eg.edu.alexu.cse.mail_server.dto.AttachmentDTO;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MailService {
    private final SendCommand sendCommand;
    private final DraftCommand draftCommand;
    private final GetMailCommand getMailCommand;
    private final MailRepository mailRepository;
    private final AttachmentService attachmentService;

    public void send(ComposeEmailDTO composeEmailDTO) {
        sendCommand.execute(composeEmailDTO);
    }

    /**
     * Send email with attachments
     * @param composeEmailDTO email details
     * @param attachments list of files to attach
     * @throws IOException if file processing fails
     */
    public void sendWithAttachments(ComposeEmailDTO composeEmailDTO, List<MultipartFile> attachments) throws IOException {
        sendCommand.executeWithAttachments(composeEmailDTO, attachments);
    }

    public void draft(ComposeEmailDTO composeEmailDTO) {
        draftCommand.execute(composeEmailDTO);
    }

    // Get inbox mails
    public List<EmailViewDto> getInboxMails(String userEmail) {
        List<Mail> mails = mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, "INBOX");
        return mails.stream().map(this::convertToEmailViewDto).collect(Collectors.toList());
    }

    // Get sent mails
    public List<EmailViewDto> getSentMails(String userEmail) {
        List<Mail> mails = mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "SENT");
        return mails.stream().map(this::convertToEmailViewDto).collect(Collectors.toList());
    }

    // Get draft mails
    public List<EmailViewDto> getDraftMails(String userEmail) {
        List<Mail> mails = mailRepository.findBySenderAndFolderNameOrderByTimestampDesc(userEmail, "DRAFTS");
        return mails.stream().map(this::convertToEmailViewDto).collect(Collectors.toList());
    }

    // Get mails by folder
    public List<EmailViewDto> getMailsByFolder(String userEmail, String folderName) {
        List<Mail> mails;
        if ("all".equals(folderName)) {
            mails = mailRepository.findByReceiverOrSenderOrderByTimestampDesc(userEmail, userEmail);
        } else {
            mails = mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, folderName);
        }
        return mails.stream().map(this::convertToEmailViewDto).collect(Collectors.toList());
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
        mail.setDeletedAt(java.time.LocalDateTime.now()); // Track when moved to trash
        mailRepository.save(mail);
    }

    /**
     * Permanently delete emails that have been in trash for more than 30 days
     * Called by scheduled task
     */
    public void deleteOldTrashEmails() {
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        List<Mail> oldTrashMails = mailRepository.findByFolderNameAndDeletedAtBefore("trash", thirtyDaysAgo);

        if (!oldTrashMails.isEmpty()) {
            mailRepository.deleteAll(oldTrashMails);
            System.out.println("Deleted " + oldTrashMails.size() + " old emails from trash");
        }
    }

    /**
     * Copy an email to a custom folder
     * Creates a duplicate of the email with the specified folder name
     * @param mailId ID of the email to copy
     * @param folderName Name of the target folder
     */
    public void copyEmailToFolder(Long mailId, String folderName) {
        // Get original email
        Mail originalMail = getMailById(mailId);

        // Validate folder name
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }

        // Create a copy of the email
        Mail copiedMail = Mail.builder()
                .sender(originalMail.getSender())
                .senderRel(originalMail.getSenderRel())
                .receiver(originalMail.getReceiver())
                .subject(originalMail.getSubject())
                .body(originalMail.getBody())
                .priority(originalMail.getPriority())
                .timestamp(java.time.LocalDateTime.now()) // New timestamp for the copy
                .folderName(folderName.toLowerCase()) // Store folder name in lowercase
                .isRead(originalMail.isRead())
                .receiverRel(originalMail.getReceiverRel())
                .attachments(originalMail.getAttachments()) // Reference same attachments
                .build();

        mailRepository.save(copiedMail);
    }

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

    /**
     * Convert Mail entity to EmailViewDto with FULL attachment data (Base64 encoded)
     * Loads all attachment files and encodes them for frontend
     */
    private EmailViewDto convertToEmailViewDto(Mail mail) {
        // Load full attachment data with Base64 encoding
        List<AttachmentDTO> attachmentDTOs = null;
        if (mail.getAttachments() != null && !mail.getAttachments().isEmpty()) {
            attachmentDTOs = new ArrayList<>();
            for (Attachment attachment : mail.getAttachments()) {
                try {
                    // Read file from disk
                    byte[] fileData = attachmentService.readAttachmentFile(attachment.getFilePath());

                    // Encode to Base64
                    String base64Data = Base64.getEncoder().encodeToString(fileData);

                    // Create DTO with full data
                    AttachmentDTO dto = AttachmentDTO.builder()
                            .id(attachment.getId())
                            .fileName(attachment.getFileName())
                            .contentType(attachment.getContentType())
                            .fileSize(attachment.getFileSize())
                            .fileData(base64Data)
                            .build();

                    attachmentDTOs.add(dto);
                } catch (IOException e) {
                    // Log error but continue with other attachments
                    System.err.println("Failed to load attachment " + attachment.getId() + ": " + e.getMessage());
                }
            }
        }

        return EmailViewDto.builder()
                .id(mail.getMailId())
                .sender(mail.getSender())
                .receiver(mail.getReceiver())
                .subject(mail.getSubject())
                .body(mail.getBody())
                .timestamp(mail.getTimestamp())
                .priority(mail.getPriority())
                .folderName(mail.getFolderName())
                .isRead(mail.isRead())
                .attachments(attachmentDTOs)           // Full attachment data with Base64
                .build();
    }

    public List<Mail> getSortedMails(String critera, boolean order){
        switch(critera){
            case:
        }
    }

}
