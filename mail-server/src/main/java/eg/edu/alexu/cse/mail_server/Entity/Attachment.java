package eg.edu.alexu.cse.mail_server.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_path", unique = true)
    private String filePath; // Path to file on disk

    @Column(name = "indexed_content", columnDefinition = "TEXT")
    private String indexedContent; // For searching

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id")
    private Mail mail;
}
