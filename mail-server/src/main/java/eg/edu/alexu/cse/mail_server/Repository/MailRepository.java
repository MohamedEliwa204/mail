package eg.edu.alexu.cse.mail_server.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eg.edu.alexu.cse.mail_server.Entity.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByReceiverAndFolderName(String receiver, String folderName);

    List<Mail> findBySenderAndFolderName(String sender, String folderName);

    List<Mail> findByReceiverAndSubjectContainingIgnoreCase(String receiver, String subject);

    List<Mail> findByReceiverAndSenderContainingIgnoreCase(String receiver, String sender);
}
