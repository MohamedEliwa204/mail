package eg.edu.alexu.cse.mail_server.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mail {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailId;

    @Column(nullable = false)
    private String sender;
    @Column(nullable = false)
    private String receiver;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private String subject;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private int priority = 1;

    @Column(nullable = false)
    private String folderName; // inbox, sent, trash, draft

    @Builder.Default
    private boolean isRead = false; // for ui

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id")
    private List<Attachment> attachments;

}
