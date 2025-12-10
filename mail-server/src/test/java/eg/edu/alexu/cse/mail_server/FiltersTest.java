package eg.edu.alexu.cse.mail_server;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import eg.edu.alexu.cse.mail_server.Service.Strategy.* ;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FiltersTest {

    private User user1, user2;
    private Mail mail1, mail2, mail3;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.now();

        // Users
        user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");

        user2 = new User();
        user2.setFirstName("Alice");
        user2.setLastName("Smith");

        // Mails
        mail1 = new Mail();
        mail1.setSenderRel(user1);
        mail1.setSubject("Meeting Tomorrow");
        mail1.setBody("We will discuss the project plan");
        mail1.setPriority(3);
        mail1.setRead(false);
        mail1.setTimestamp(now.minusDays(1));

        mail2 = new Mail();
        mail2.setSenderRel(user2);
        mail2.setSubject("Project Update");
        mail2.setBody("The project is delayed by 2 weeks");
        mail2.setPriority(5);
        mail2.setRead(true);
        mail2.setTimestamp(now.minusDays(40));

        mail3 = new Mail();
        mail3.setSenderRel(user1);
        mail3.setSubject("Urgent: Server Down");
        mail3.setBody("Server is not responding");
        mail3.setPriority(0);
        mail3.setRead(true);
        mail3.setTimestamp(now.minusDays(100));
    }

    @Test
    void testSenderFilter() {
        SenderFilter filter = new SenderFilter("John Doe");
        assertTrue(filter.filter(mail1));
        assertEquals(100, filter.getScore(mail1));
        assertFalse(filter.filter(mail2));
        assertEquals(0, filter.getScore(mail2));
    }

    @Test
    void testSubjectFilter() {
        SubjectFilter filter = new SubjectFilter("Project");
        assertTrue(filter.filter(mail2));
        assertTrue(filter.getScore(mail2) > 0);
        assertFalse(filter.filter(mail1));
        assertEquals(0, filter.getScore(mail1));
    }

    @Test
    void testBodyFilter() {
        BodyFilter filter = new BodyFilter("project plan");
        assertTrue(filter.filter(mail1));
        assertTrue(filter.getScore(mail1) > 0);
        assertTrue(filter.filter(mail2));
    }

    @Test
    void testPriorityFilter() {
        PriorityFilter filter = new PriorityFilter(3);
        assertTrue(filter.filter(mail1)); // exact match
        assertEquals(100, filter.getScore(mail1));

        assertTrue(filter.filter(mail2)); // difference 2
        assertEquals(50, filter.getScore(mail2));

        assertFalse(filter.filter(mail3)); // difference > 2
        assertEquals(0, filter.getScore(mail3));
    }

    @Test
    void testIsReadFilter() {
        IsReadFilter filter = new IsReadFilter();
        filter.setRead(true);
        assertFalse(filter.filter(mail1));
        assertTrue(filter.filter(mail2));
        assertEquals(100, filter.getScore(mail2));
    }

    @Test
    void testExactDateFilter() {
        ExactDateFilter filter = new ExactDateFilter(now.minusDays(1));
        assertTrue(filter.filter(mail1));
        assertFalse(filter.filter(mail2));
        assertEquals(100, filter.getScore(mail1));
        assertEquals(0, filter.getScore(mail2));
    }

    @Test
    void testBeforeDateFilter() {
        BeforeDateFilter filter = new BeforeDateFilter();
        filter.setDate(now.minusDays(10));
        assertTrue(filter.filter(mail2));
        assertFalse(filter.filter(mail1));
        System.out.println("BeforeDateFilter Scores:");
        System.out.println("mail1: " + filter.getScore(mail1));
        System.out.println("mail2: " + filter.getScore(mail2));
        System.out.println("mail3: " + filter.getScore(mail3));
    }

    @Test
    void testAfterDateFilter() {
        AfterDataFilter filter = new AfterDataFilter(now.minusDays(30));
        assertTrue(filter.filter(mail1));
        assertFalse(filter.filter(mail3));
        System.out.println("AfterDateFilter Scores:");
        System.out.println("mail1: " + filter.getScore(mail1));
        System.out.println("mail2: " + filter.getScore(mail2));
        System.out.println("mail3: " + filter.getScore(mail3));
    }

    @Test
    void testCombinedFilter() {
        List<Mail> allMails = List.of(mail1, mail2, mail3);

        // Example: Filter mails from John Doe AND containing 'Server'
        SenderFilter senderFilter = new SenderFilter("John Doe");
        SubjectFilter subjectFilter = new SubjectFilter("Server");

        // Simple manual AND combination
        List<Mail> filtered = allMails.stream()
                .filter(m -> senderFilter.filter(m) && subjectFilter.filter(m))
                .toList();

        assertEquals(1, filtered.size());
        assertEquals(mail3, filtered.getFirst());
        System.out.println("Combined score: " + Math.min(senderFilter.getScore(mail3), subjectFilter.getScore(mail3)));
    }
}

