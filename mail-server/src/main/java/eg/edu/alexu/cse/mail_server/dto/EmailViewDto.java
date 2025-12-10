package eg.edu.alexu.cse.mail_server.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmailViewDto {
    private Long id;
    private String sender;
    private String subject;
    private String body;
    private LocalDateTime timestamp;
    private int priority;
    private boolean isRead;

    // Full attachment data with file content
    private List<AttachmentDTO> attachments;

    // No-args constructor required for Jackson deserialization
    public EmailViewDto() {
    }
}
