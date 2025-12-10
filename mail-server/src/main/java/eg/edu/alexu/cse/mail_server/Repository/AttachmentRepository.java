package eg.edu.alexu.cse.mail_server.Repository;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Find all attachments by mail ID
     * Uses the 'mail' field in Attachment entity and accesses its 'mailId' property
     */
    List<Attachment> findByMailMailId(Long mailId);
}
