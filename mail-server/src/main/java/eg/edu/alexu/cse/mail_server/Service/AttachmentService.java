package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.AttachmentRepository;
import eg.edu.alexu.cse.mail_server.dto.AttachmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AttachmentService {
    private static final String UPLOAD_DIR = "attachments/";
    private final AttachmentRepository attachmentRepository;

    public Attachment saveAttachment(MultipartFile file, Mail mail) throws IOException {

        String uniqueFileName = UUID.randomUUID().toString() + "_" +
                file.getOriginalFilename();


        String filePath = UPLOAD_DIR + mail.getMailId() + "/" + uniqueFileName;
        Path path = Paths.get(filePath);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());


        String indexedContent = extractTextContent(file);

        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .indexedContent(indexedContent)
                .uploadDate(LocalDateTime.now())
                .mail(mail)
                .build();

        return attachmentRepository.save(attachment);
    }

    /**
     * Get all attachments for a specific mail
     *
     * @param mailId the ID of the mail
     * @return list of all attachments belonging to this mail
     */
    public List<Attachment> getAttachmentsByMailId(Long mailId) {
        return attachmentRepository.findByMailMailId(mailId);
    }

    /**
     * Get all attachments with file data for a specific mail
     * This method reads the actual file content and encodes it as Base64
     * for sending to the frontend
     *
     * @param mailId the ID of the mail
     * @return list of AttachmentDTOs with file data included
     * @throws IOException if file reading fails
     */
    public List<AttachmentDTO> getAttachmentsWithData(Long mailId) throws IOException {
        List<Attachment> attachments = getAttachmentsByMailId(mailId);
        List<AttachmentDTO> attachmentDTOs = new ArrayList<>();

        for (Attachment attachment : attachments) {

            byte[] fileData = readAttachmentFile(attachment.getFilePath());

            // Encode to Base64 for JSON transfer
            String base64Data = java.util.Base64.getEncoder().encodeToString(fileData);

            AttachmentDTO dto = AttachmentDTO.builder()
                    .id(attachment.getId())
                    .fileName(attachment.getFileName())
                    .contentType(attachment.getContentType())
                    .fileSize(attachment.getFileSize())
                    .fileData(base64Data)
                    .build();

            attachmentDTOs.add(dto);
        }

        return attachmentDTOs;
    }

    /**
     * Get attachment data by attachment ID
     * (Optional - for downloading specific attachment)
     */
    public byte[] getAttachmentData(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        Path path = Paths.get(attachment.getFilePath());
        return Files.readAllBytes(path);
    }

    /**
     * Read file data from disk by file path
     *
     * @param filePath the path to the file on disk
     * @return file content as byte array
     */
    public byte[] readAttachmentFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    private String extractTextContent(MultipartFile file) {

        return "";
    }

}
