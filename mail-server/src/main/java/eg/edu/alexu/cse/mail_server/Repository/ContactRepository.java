package eg.edu.alexu.cse.mail_server.Repository;

import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByName(String name);

    List<Contact> deleteByName(String name);

    List<Contact> findByUser_Email(String email);

    List<Contact> findByUser_EmailOrderByNameDesc(String email);

    List<Contact> findByUser_EmailOrderByNameAsc(String email);

}
