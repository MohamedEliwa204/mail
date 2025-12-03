package eg.edu.alexu.cse.mail_server.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmailViewDto {
    private Long id;
    private String sender;
    private String subject;
    private String body;
    private LocalDateTime timestamp;
    private int priority;
    private boolean isRead;

    // We only send the file NAMES to the list, not the actual file data (BLOB)
    // Clicking the name will trigger a separate download request.
    private List<String> attachmentNames;
}
