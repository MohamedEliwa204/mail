package eg.edu.alexu.cse.mail_server.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eg.edu.alexu.cse.mail_server.Entity.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByReceiverAndFolderName(String receiver, String folderName);

    List<Mail> findBySenderAndFolderName(String sender, String folderName);

    List<Mail> findByReceiverAndSubjectContainingIgnoreCase(String receiver, String subject);

    List<Mail> findByReceiverAndSenderContainingIgnoreCase(String receiver, String sender);

    // Sorted versions for API endpoints
    List<Mail> findByReceiverAndFolderNameOrderByTimestampDesc(String receiver, String folderName);

    List<Mail> findBySenderAndFolderNameOrderByTimestampDesc(String sender, String folderName);
    // Find all emails where the user is either sender or receiver
    @Query("SELECT m FROM Mail m WHERE m.senderRel.userId = :userId OR :userId IN (SELECT r.userId FROM m.receiverRel r)")
    List<Mail> findAllByUserId(@Param("userId") Long userId);


    @Query("SELECT m FROM Mail m WHERE m.receiver = :email OR m.sender = :email ORDER BY m.timestamp DESC")
    List<Mail> findByReceiverOrSenderOrderByTimestampDesc(@Param("email") String email1, @Param("email") String email2);
}
