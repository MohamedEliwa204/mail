package eg.edu.alexu.cse.mail_server;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Service.Decorator.AndDecorator;
import eg.edu.alexu.cse.mail_server.Service.Decorator.OrDecorator;
import eg.edu.alexu.cse.mail_server.Service.Strategy.MailFilter;
import eg.edu.alexu.cse.mail_server.Service.Strategy.SenderFilter;
import eg.edu.alexu.cse.mail_server.Service.Strategy.SubjectFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class MailFilterTest {

    private User alice;
    private User bob;
    private Mail mail1;
    private Mail mail2;
    private Mail mail3;
    private List<Mail> allMails;

    @BeforeEach
    void setUp() {
        // --- Create users using no-arg constructor + setters ---
        alice = new User();
        alice.setFirstName("Alice");
        alice.setLastName("Smith");
        alice.setEmail("alice@mail.com");

        bob = new User();
        bob.setFirstName("Bob");
        bob.setLastName("Johnson");
        bob.setEmail("bob@mail.com");

        // --- Create mail 1 ---
        mail1 = new Mail();
        mail1.setSenderRel(alice);
        mail1.setSubject("Project deadline");
        mail1.setBody("Details about the project");
        mail1.setTimestamp(LocalDateTime.now());

        // --- Create mail 2 ---
        mail2 = new Mail();
        mail2.setSenderRel(bob);
        mail2.setSubject("Lunch tomorrow");
        mail2.setBody("Are you free for lunch?");
        mail2.setTimestamp(LocalDateTime.now());

        // --- Create mail 3 ---
        mail3 = new Mail();
        mail3.setSenderRel(alice);
        mail3.setSubject("Invoice for client");
        mail3.setBody("Please see attached invoice");
        mail3.setTimestamp(LocalDateTime.now());

        // --- Put all mails in a list ---
        allMails = List.of(mail1, mail2, mail3);
    }
    @Test
    void testNameFilter_firstNameOnly() {
        SenderFilter filter = new SenderFilter("Alice");
        MailFilter searcher = new MailFilter(filter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(2, result.size()); // mail1 and mail3
        assertTrue(result.contains(mail1));
        assertTrue(result.contains(mail3));
    }

    @Test
    void testNameFilter_lastNameOnly() {
        SenderFilter filter = new SenderFilter("Johnson");
        MailFilter searcher = new MailFilter(filter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(1, result.size()); // mail2
        assertTrue(result.contains(mail2));
    }

    @Test
    void testSubjectFilter_singleKeyword() {
        SubjectFilter filter = new SubjectFilter("invoice");
        MailFilter searcher = new MailFilter(filter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(1, result.size()); // mail3
        assertTrue(result.contains(mail3));
    }

    @Test
    void testSubjectFilter_multipleKeywords() {
        SubjectFilter filter = new SubjectFilter("project lunch");
        MailFilter searcher = new MailFilter(filter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(2, result.size()); // mail1 and mail2
        assertTrue(result.contains(mail1));
        assertTrue(result.contains(mail2));
    }

    // --- AND Decorator Test ---
    @Test
    void testAndDecorator() {
        SenderFilter senderFilter = new SenderFilter("Alice");
        SubjectFilter subjectFilter = new SubjectFilter("invoice");

        AndDecorator andFilter = new AndDecorator(senderFilter, subjectFilter);
        MailFilter searcher = new MailFilter(andFilter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(1, result.size()); // Only mail3 matches both
        assertTrue(result.contains(mail3));
    }

    // --- OR Decorator Test ---
    @Test
    void testOrDecorator() {
        SenderFilter senderFilter = new SenderFilter("Alice");
        SubjectFilter subjectFilter = new SubjectFilter("lunch");

        OrDecorator orFilter = new OrDecorator(senderFilter, subjectFilter);
        MailFilter searcher = new MailFilter(orFilter);

        List<Mail> result = searcher.getEmails(allMails);
        assertEquals(3, result.size()); // mail1, mail2, mail3
        assertTrue(result.contains(mail1));
        assertTrue(result.contains(mail2));
        assertTrue(result.contains(mail3));
    }


}

