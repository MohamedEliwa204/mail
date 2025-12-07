package eg.edu.alexu.cse.mail_server;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Service.Decorator.AndDecorator;
import eg.edu.alexu.cse.mail_server.Service.Decorator.OrDecorator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import eg.edu.alexu.cse.mail_server.Service.Strategy.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("🔥 Spicy Mail Filter Test Suite 🔥")
class MailFilterSpicyTest {

    private List<Mail> testMails;
    private User johnDoe, janeDoe, bobSmith, aliceWonder;

    @BeforeEach
    void setUp() {
        // Create test users
        johnDoe = createUser(1L, "John", "Doe", "john.doe@test.com");
        janeDoe = createUser(2L, "Jane", "Doe", "jane.doe@test.com");
        bobSmith = createUser(3L, "Bob", "Smith", "bob.smith@test.com");
        aliceWonder = createUser(4L, "Alice", "Wonder", "alice.wonder@test.com");

        // Create diverse test emails
        testMails = new ArrayList<>();

        // Email 1: Urgent meeting from John
        testMails.add(createMail(1L, johnDoe, "Urgent: Team Meeting Tomorrow",
                "Please attend the important team meeting scheduled for tomorrow at 10 AM.",
                5, true, LocalDateTime.now().minusDays(1)));

        // Email 2: Project update from Jane
        testMails.add(createMail(2L, janeDoe, "Project Update - Q4 Progress",
                "Here's the quarterly progress report for our main project.",
                3, false, LocalDateTime.now().minusDays(7)));

        // Email 3: Casual lunch invite from Bob
        testMails.add(createMail(3L, bobSmith, "Lunch tomorrow?",
                "Hey, want to grab lunch tomorrow? I found a great new restaurant.",
                2, false, LocalDateTime.now().minusDays(2)));

        // Email 4: Important deadline from Alice
        testMails.add(createMail(4L, aliceWonder, "URGENT: Project Deadline Extended",
                "Good news! The project deadline has been extended by one week.",
                5, true, LocalDateTime.now().minusDays(3)));

        // Email 5: Old newsletter
        testMails.add(createMail(5L, johnDoe, "Monthly Newsletter - September",
                "Check out this month's newsletter with company updates and achievements.",
                1, false, LocalDateTime.now().minusDays(60)));

        // Email 6: Recent meeting notes from Jane
        testMails.add(createMail(6L, janeDoe, "Meeting Notes: Strategy Session",
                "Attached are the notes from yesterday's strategy meeting. Key decisions included...",
                3, false, LocalDateTime.now().minusHours(12)));

        // Email 7: Unread spam-like email
        testMails.add(createMail(7L, bobSmith, "You won't believe this!",
                "Amazing opportunity just for you! Click here now!",
                1, false, LocalDateTime.now().minusDays(5)));

        // Email 8: Critical system alert
        testMails.add(createMail(8L, aliceWonder, "CRITICAL: System Maintenance Tonight",
                "The system will undergo critical maintenance tonight from 11 PM to 3 AM.",
                5, false, LocalDateTime.now().minusHours(2)));
    }

    // === INDIVIDUAL FILTER TESTS ===

    @Nested
    @DisplayName("🎯 Subject Filter Tests")
    class SubjectFilterTests {

        @Test
        @DisplayName("Should find emails with exact subject match")
        void testExactSubjectMatch() {
            SubjectFilter filter = new SubjectFilter("Monthly Newsletter - September");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            // Since SubjectFilter performs OR search, it will match "Monthly" OR "Newsletter" OR "September"
            // But only one email should have the exact match with score 100
            assertTrue(results.size() >= 1, "Should find at least one matching email");

            // Find the exact match by checking score
            Mail exactMatch = results.stream()
                    .filter(m -> filter.getScore(m) == 100)
                    .findFirst()
                    .orElse(null);

            assertNotNull(exactMatch, "Should find an exact match with score 100");
            assertEquals("Monthly Newsletter - September", exactMatch.getSubject());
            assertEquals(100, filter.getScore(exactMatch), "Exact match should score 100");
        }

        @Test
        @DisplayName("Should find emails with partial word matches (OR logic)")
        void testPartialWordMatches() {
            SubjectFilter filter = new SubjectFilter("urgent meeting");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() >= 3); // Should find emails with "urgent" OR "meeting"
            assertTrue(results.stream().anyMatch(m -> m.getSubject().toLowerCase().contains("urgent")));
            assertTrue(results.stream().anyMatch(m -> m.getSubject().toLowerCase().contains("meeting")));
        }

        @Test
        @DisplayName("Should handle case-insensitive searches")
        void testCaseInsensitiveSearch() {
            SubjectFilter filter1 = new SubjectFilter("URGENT");
            SubjectFilter filter2 = new SubjectFilter("urgent");

            assertEquals(
                    new MailFilter(filter1).getEmails(testMails).size(),
                    new MailFilter(filter2).getEmails(testMails).size()
            );
        }

        @Test
        @DisplayName("Should score results by relevance")
        void testScoring() {
            SubjectFilter filter = new SubjectFilter("project deadline");

            Mail exactMatch = testMails.stream()
                    .filter(m -> m.getSubject().toLowerCase().contains("project") &&
                            m.getSubject().toLowerCase().contains("deadline"))
                    .findFirst().orElse(null);

            Mail partialMatch = testMails.stream()
                    .filter(m -> m.getSubject().toLowerCase().contains("project") &&
                            !m.getSubject().toLowerCase().contains("deadline"))
                    .findFirst().orElse(null);

            assertNotNull(exactMatch);
            assertNotNull(partialMatch);
            assertTrue(filter.getScore(exactMatch) > filter.getScore(partialMatch));
        }
    }

    @Nested
    @DisplayName("👤 Sender Filter Tests")
    class SenderFilterTests {

        @Test
        @DisplayName("Should find emails by first name")
        void testFindByFirstName() {
            SenderFilter filter = new SenderFilter("John");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() >= 2);
            assertTrue(results.stream().allMatch(m -> m.getSenderRel().getFirstName().equals("John")));
        }

        @Test
        @DisplayName("Should find emails by last name")
        void testFindByLastName() {
            SenderFilter filter = new SenderFilter("Doe");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() >= 2);
            assertTrue(results.stream().allMatch(m -> m.getSenderRel().getLastName().equals("Doe")));
        }

        @Test
        @DisplayName("Should find emails by full name with space")
        void testFindByFullName() {
            SenderFilter filter = new SenderFilter("Jane Doe");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() >= 1);
            assertEquals(100, filter.getScore(results.get(0)));
        }

        @Test
        @DisplayName("Should find emails by full name without space")
        void testFindByFullNameNoSpace() {
            SenderFilter filter = new SenderFilter("JaneDoe");

            assertTrue(filter.filter(testMails.stream()
                    .filter(m -> m.getSenderRel().getFirstName().equals("Jane"))
                    .findFirst().get()));
        }

        @Test
        @DisplayName("Should handle reversed name order")
        void testReversedNameOrder() {
            SenderFilter filter = new SenderFilter("Smith Bob");

            assertTrue(filter.filter(testMails.stream()
                    .filter(m -> m.getSenderRel().getFirstName().equals("Bob"))
                    .findFirst().get()));
        }
    }

    @Nested
    @DisplayName("⏰ Date Filter Tests")
    class DateFilterTests {

        @Test
        @DisplayName("Should find emails before specific date")
        void testBeforeDateFilter() {
            BeforeDateFilter filter = new BeforeDateFilter(LocalDateTime.now().minusDays(5));
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(m -> m.getTimestamp().isBefore(LocalDateTime.now().minusDays(5))));
        }

        @Test
        @DisplayName("Should find emails after specific date")
        void testAfterDateFilter() {
            AfterDataFilter filter = new AfterDataFilter(LocalDateTime.now().minusDays(5));
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(m -> m.getTimestamp().isAfter(LocalDateTime.now().minusDays(5))));
        }

        @Test
        @DisplayName("Should find emails with exact date match")
        void testExactDateFilter() {
            LocalDateTime targetDate = testMails.get(0).getTimestamp();
            ExactDateFilter filter = new ExactDateFilter(targetDate);
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertEquals(1, results.size());
            assertEquals(targetDate, results.get(0).getTimestamp());
        }

        @Test
        @DisplayName("Should score closer dates higher (Before)")
        void testBeforeDateScoring() {
            LocalDateTime cutoff = LocalDateTime.now();
            BeforeDateFilter filter = new BeforeDateFilter(cutoff);

            Mail recentMail = testMails.stream()
                    .filter(m -> m.getTimestamp().isBefore(cutoff))
                    .min((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                    .get();

            Mail oldMail = testMails.stream()
                    .filter(m -> m.getTimestamp().isBefore(cutoff))
                    .min((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                    .get();

            assertTrue(filter.getScore(recentMail) > filter.getScore(oldMail));
        }
    }

    @Nested
    @DisplayName("📊 Priority Filter Tests")
    class PriorityFilterTests {

        @Test
        @DisplayName("Should find high priority emails (exact match)")
        void testHighPriorityExact() {
            PriorityFilter filter = new PriorityFilter(5);
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertEquals(100, filter.getScore(results.get(0))); // Exact match should score 100
        }

        @Test
        @DisplayName("Should include emails with close priority (±2)")
        void testPriorityRange() {
            PriorityFilter filter = new PriorityFilter(3);
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m -> Math.abs(m.getPriority() - 3) <= 2));
        }

        @Test
        @DisplayName("Should score by priority proximity")
        void testPriorityScoring() {
            PriorityFilter filter = new PriorityFilter(5);

            Mail exactMatch = testMails.stream().filter(m -> m.getPriority() == 5).findFirst().get();
            Mail closeMatch = testMails.stream().filter(m -> m.getPriority() == 3).findFirst().get();

            assertTrue(filter.getScore(exactMatch) > filter.getScore(closeMatch));
        }
    }

    @Nested
    @DisplayName("📖 Read Status Filter Tests")
    class ReadStatusFilterTests {

        @Test
        @DisplayName("Should find only read emails")
        void testReadEmails() {
            IsReadFilter filter = new IsReadFilter(true);
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(Mail::isRead));
        }

        @Test
        @DisplayName("Should find only unread emails")
        void testUnreadEmails() {
            IsReadFilter filter = new IsReadFilter(false);
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().noneMatch(Mail::isRead));
        }
    }

    @Nested
    @DisplayName("📝 Body Filter Tests")
    class BodyFilterTests {

        @Test
        @DisplayName("Should find emails with specific words in body")
        void testBodyKeywordSearch() {
            BodyFilter filter = new BodyFilter("meeting tomorrow");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().anyMatch(m ->
                    m.getBody().toLowerCase().contains("meeting") ||
                            m.getBody().toLowerCase().contains("tomorrow")));
        }

        @Test
        @DisplayName("Should handle empty body gracefully")
        void testEmptyBody() {
            Mail emptyBodyMail = createMail(99L, johnDoe, "No Body", null, 1, false, LocalDateTime.now());
            List<Mail> singleMailList = List.of(emptyBodyMail);

            BodyFilter filter = new BodyFilter("test");
            MailFilter mailFilter = new MailFilter(filter);

            List<Mail> results = mailFilter.getEmails(singleMailList);

            assertEquals(0, results.size());
        }
    }

    // === DECORATOR COMBINATION TESTS ===

    @Nested
    @DisplayName("🔗 AND Decorator Tests - Complex Combinations")
    class AndDecoratorTests {

        @Test
        @DisplayName("Should find urgent emails from John (AND logic)")
        void testSubjectAndSender() {
            SubjectFilter subjectFilter = new SubjectFilter("urgent");
            SenderFilter senderFilter = new SenderFilter("John");
            AndDecorator andFilter = new AndDecorator(subjectFilter, senderFilter);
            MailFilter mailFilter = new MailFilter(andFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(m ->
                    m.getSubject().toLowerCase().contains("urgent") &&
                            m.getSenderRel().getFirstName().equals("John")));
        }

        @Test
        @DisplayName("Should find high priority unread emails")
        void testPriorityAndReadStatus() {
            PriorityFilter priorityFilter = new PriorityFilter(5);
            IsReadFilter readFilter = new IsReadFilter(false);
            AndDecorator andFilter = new AndDecorator(priorityFilter, readFilter);
            MailFilter mailFilter = new MailFilter(andFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    Math.abs(m.getPriority() - 5) <= 2 && !m.isRead()));
        }

        @Test
        @DisplayName("Should find recent emails from specific sender")
        void testDateAndSender() {
            AfterDataFilter dateFilter = new AfterDataFilter(LocalDateTime.now().minusDays(3));
            SenderFilter senderFilter = new SenderFilter("Alice Wonder");
            AndDecorator andFilter = new AndDecorator(dateFilter, senderFilter);
            MailFilter mailFilter = new MailFilter(andFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    m.getTimestamp().isAfter(LocalDateTime.now().minusDays(3)) &&
                            m.getSenderRel().getFirstName().equals("Alice")));
        }

        @Test
        @DisplayName("Should handle triple AND combination")
        void testTripleAnd() {
            SubjectFilter subjectFilter = new SubjectFilter("project");
            PriorityFilter priorityFilter = new PriorityFilter(3);
            IsReadFilter readFilter = new IsReadFilter(false);

            AndDecorator firstAnd = new AndDecorator(subjectFilter, priorityFilter);
            AndDecorator secondAnd = new AndDecorator(firstAnd, readFilter);
            MailFilter mailFilter = new MailFilter(secondAnd);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    m.getSubject().toLowerCase().contains("project") &&
                            Math.abs(m.getPriority() - 3) <= 2 &&
                            !m.isRead()));
        }

        @Test
        @DisplayName("Should compute soft AND scores correctly")
        void testSoftAndScoring() {
            SubjectFilter filter1 = new SubjectFilter("urgent meeting");
            SenderFilter filter2 = new SenderFilter("John");
            AndDecorator andFilter = new AndDecorator(filter1, filter2);

            Mail testMail = testMails.get(0); // Urgent meeting from John
            int score1 = filter1.getScore(testMail);
            int score2 = filter2.getScore(testMail);
            int combinedScore = andFilter.getScore(testMail);

            // Soft AND should produce a score between min and geometric mean
            int expected = (int) Math.pow(score1 / 100.0 * score2 / 100.0, 0.5) * 100;
            assertEquals(expected, combinedScore);
        }
    }

    @Nested
    @DisplayName("🔀 OR Decorator Tests - Complex Combinations")
    class OrDecoratorTests {

        @Test
        @DisplayName("Should find emails from John OR Jane")
        void testSenderOr() {
            SenderFilter johnFilter = new SenderFilter("John");
            SenderFilter janeFilter = new SenderFilter("Jane");
            OrDecorator orFilter = new OrDecorator(johnFilter, janeFilter);
            MailFilter mailFilter = new MailFilter(orFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() >= 4);
            assertTrue(results.stream().allMatch(m ->
                    m.getSenderRel().getFirstName().equals("John") ||
                            m.getSenderRel().getFirstName().equals("Jane")));
        }

        @Test
        @DisplayName("Should find urgent OR high priority emails")
        void testSubjectOrPriority() {
            SubjectFilter subjectFilter = new SubjectFilter("urgent");
            PriorityFilter priorityFilter = new PriorityFilter(5);
            OrDecorator orFilter = new OrDecorator(subjectFilter, priorityFilter);
            MailFilter mailFilter = new MailFilter(orFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(m ->
                    m.getSubject().toLowerCase().contains("urgent") ||
                            Math.abs(m.getPriority() - 5) <= 2));
        }

        @Test
        @DisplayName("Should find emails matching any of three criteria")
        void testTripleOr() {
            SubjectFilter subjectFilter = new SubjectFilter("meeting");
            PriorityFilter priorityFilter = new PriorityFilter(1);
            IsReadFilter readFilter = new IsReadFilter(true);

            OrDecorator firstOr = new OrDecorator(subjectFilter, priorityFilter);
            OrDecorator secondOr = new OrDecorator(firstOr, readFilter);
            MailFilter mailFilter = new MailFilter(secondOr);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    m.getSubject().toLowerCase().contains("meeting") ||
                            Math.abs(m.getPriority() - 1) <= 2 ||
                            m.isRead()));
        }

        @Test
        @DisplayName("Should compute soft OR scores correctly")
        void testSoftOrScoring() {
            SubjectFilter filter1 = new SubjectFilter("project");
            PriorityFilter filter2 = new PriorityFilter(3);
            OrDecorator orFilter = new OrDecorator(filter1, filter2);

            Mail testMail = testMails.get(1); // Project update, priority 3
            int score1 = filter1.getScore(testMail);
            int score2 = filter2.getScore(testMail);
            int combinedScore = orFilter.getScore(testMail);

            // Soft OR: a + b - (a * b / 100)
            int expected = score1 + score2 - (score1 * score2 / 100);
            assertEquals(expected, combinedScore);
        }
    }

    @Nested
    @DisplayName("🌶️ Ultra Spicy Combo Tests")
    class UltraSpicyCombos {

        @Test
        @DisplayName("Should find (Urgent from John) OR (High priority from Alice)")
        void testComplexOrWithAnds() {
            // (Subject: urgent AND Sender: John) OR (Priority: 5 AND Sender: Alice)
            SubjectFilter urgentFilter = new SubjectFilter("urgent");
            SenderFilter johnFilter = new SenderFilter("John");
            AndDecorator johnUrgent = new AndDecorator(urgentFilter, johnFilter);

            PriorityFilter highPriority = new PriorityFilter(5);
            SenderFilter aliceFilter = new SenderFilter("Alice");
            AndDecorator aliceHighPriority = new AndDecorator(highPriority, aliceFilter);

            OrDecorator finalFilter = new OrDecorator(johnUrgent, aliceHighPriority);
            MailFilter mailFilter = new MailFilter(finalFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.size() > 0);
            assertTrue(results.stream().allMatch(m ->
                    (m.getSubject().toLowerCase().contains("urgent") && m.getSenderRel().getFirstName().equals("John")) ||
                            (Math.abs(m.getPriority() - 5) <= 2 && m.getSenderRel().getFirstName().equals("Alice"))));
        }

        @Test
        @DisplayName("Should find unread, recent, high-priority emails from specific senders")
        void testQuadrupleAndCombo() {
            IsReadFilter unread = new IsReadFilter(false);
            AfterDataFilter recent = new AfterDataFilter(LocalDateTime.now().minusDays(4));
            PriorityFilter highPriority = new PriorityFilter(5);
            SenderFilter aliceFilter = new SenderFilter("Alice");

            AndDecorator step1 = new AndDecorator(unread, recent);
            AndDecorator step2 = new AndDecorator(step1, highPriority);
            AndDecorator finalFilter = new AndDecorator(step2, aliceFilter);
            MailFilter mailFilter = new MailFilter(finalFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    !m.isRead() &&
                            m.getTimestamp().isAfter(LocalDateTime.now().minusDays(4)) &&
                            Math.abs(m.getPriority() - 5) <= 2 &&
                            m.getSenderRel().getFirstName().equals("Alice")));
        }

        @Test
        @DisplayName("Should handle (A AND B) OR (C AND D) pattern")
        void testAndOrAndPattern() {
            // (Subject: meeting AND Read: true) OR (Priority: 5 AND Read: false)
            SubjectFilter meetingFilter = new SubjectFilter("meeting");
            IsReadFilter readFilter = new IsReadFilter(true);
            AndDecorator readMeetings = new AndDecorator(meetingFilter, readFilter);

            PriorityFilter urgentFilter = new PriorityFilter(5);
            IsReadFilter unreadFilter = new IsReadFilter(false);
            AndDecorator unreadUrgent = new AndDecorator(urgentFilter, unreadFilter);

            OrDecorator finalFilter = new OrDecorator(readMeetings, unreadUrgent);
            MailFilter mailFilter = new MailFilter(finalFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    (m.getSubject().toLowerCase().contains("meeting") && m.isRead()) ||
                            (Math.abs(m.getPriority() - 5) <= 2 && !m.isRead())));
        }

        @Test
        @DisplayName("Should find emails matching body content OR subject with date range")
        void testBodyOrSubjectWithDateRange() {
            BodyFilter bodyFilter = new BodyFilter("deadline project");
            SubjectFilter subjectFilter = new SubjectFilter("urgent meeting");
            OrDecorator contentOr = new OrDecorator(bodyFilter, subjectFilter);

            AfterDataFilter recentFilter = new AfterDataFilter(LocalDateTime.now().minusDays(10));
            AndDecorator finalFilter = new AndDecorator(contentOr, recentFilter);
            MailFilter mailFilter = new MailFilter(finalFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertTrue(results.stream().allMatch(m ->
                    m.getTimestamp().isAfter(LocalDateTime.now().minusDays(10)) &&
                            (m.getBody().toLowerCase().contains("deadline") ||
                                    m.getBody().toLowerCase().contains("project") ||
                                    m.getSubject().toLowerCase().contains("urgent") ||
                                    m.getSubject().toLowerCase().contains("meeting"))));
        }

        @Test
        @DisplayName("Should handle empty result sets gracefully")
        void testImpossibleCombination() {
            // Read emails AND Unread emails (impossible)
            IsReadFilter readFilter = new IsReadFilter(true);
            IsReadFilter unreadFilter = new IsReadFilter(false);
            AndDecorator impossibleFilter = new AndDecorator(readFilter, unreadFilter);
            MailFilter mailFilter = new MailFilter(impossibleFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            assertEquals(0, results.size());
        }

        @Test
        @DisplayName("Should score complex nested filters correctly")
        void testNestedFilterScoring() {
            SubjectFilter subjectFilter = new SubjectFilter("urgent");
            PriorityFilter priorityFilter = new PriorityFilter(5);
            AndDecorator andFilter = new AndDecorator(subjectFilter, priorityFilter);

            SenderFilter senderFilter = new SenderFilter("Alice");
            OrDecorator orFilter = new OrDecorator(andFilter, senderFilter);
            MailFilter mailFilter = new MailFilter(orFilter);

            List<Mail> results = mailFilter.getEmails(testMails);

            // Verify results are sorted by score
            for (int i = 0; i < results.size() - 1; i++) {
                int score1 = orFilter.getScore(results.get(i));
                int score2 = orFilter.getScore(results.get(i + 1));
                assertTrue(score1 >= score2, "Results should be sorted by score descending");
            }
        }
    }

    // === HELPER METHODS ===

    private User createUser(Long id, String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    private Mail createMail(Long id, User sender, String subject, String body,
                            int priority, boolean isRead, LocalDateTime timestamp) {
        Mail mail = new Mail();
        mail.setSenderRel(sender);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setPriority(priority);
        mail.setRead(isRead);
        mail.setTimestamp(timestamp);
        return mail;
    }
}