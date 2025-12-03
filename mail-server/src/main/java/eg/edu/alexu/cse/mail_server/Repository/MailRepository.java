package eg.edu.alexu.cse.mail_server.Repository;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {
}
