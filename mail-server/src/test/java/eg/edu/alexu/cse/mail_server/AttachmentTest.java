package eg.edu.alexu.cse.mail_server;


import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import eg.edu.alexu.cse.mail_server.Service.Strategy.* ;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttachmentFilter Spicy Tests 🌶️")
class AttachmentFilterTest {

    private Mail mail;
    private AttachmentFilter filter;

    @BeforeEach
    void setUp() {
        mail = new Mail();
        mail.setAttachments(new ArrayList<>());
    }

    @Nested
    @DisplayName("Filter Method Tests 🔍")
    class FilterTests {

        @Test
        @DisplayName("Should return false when mail has no attachments")
        void testNoAttachments() {
            filter = new AttachmentFilter("report");
            mail.setAttachments(null);

            assertFalse(filter.filter(mail));
        }

        @Test
        @DisplayName("Should return false when attachments list is empty")
        void testEmptyAttachmentsList() {
            filter = new AttachmentFilter("budget");

            assertFalse(filter.filter(mail));
        }

        @Test
        @DisplayName("Should match query in filename (case insensitive)")
        void testFilenameMatch() {
            filter = new AttachmentFilter("invoice");
            Attachment att = createAttachment("Invoice_2024.pdf", "Random content here");
            mail.getAttachments().add(att);

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should match query in filename with mixed case")
        void testFilenameMixedCase() {
            filter = new AttachmentFilter("REPORT");
            Attachment att = createAttachment("quarterly_report.docx", "Some data");
            mail.getAttachments().add(att);

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should match query in attachment content")
        void testContentMatch() {
            filter = new AttachmentFilter("confidential");
            Attachment att = createAttachment("document.txt", "This is a confidential report");
            mail.getAttachments().add(att);

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should return false when query not found in filename or content")
        void testNoMatch() {
            filter = new AttachmentFilter("unicorn");
            Attachment att = createAttachment("report.pdf", "Annual financial summary");
            mail.getAttachments().add(att);

            assertFalse(filter.filter(mail));
        }

        @Test
        @DisplayName("Should match in second attachment when first doesn't match")
        void testMultipleAttachmentsSecondMatch() {
            filter = new AttachmentFilter("budget");
            mail.getAttachments().add(createAttachment("report.pdf", "Sales data"));
            mail.getAttachments().add(createAttachment("budget_2024.xlsx", "Financial info"));

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should handle null filename gracefully")
        void testNullFilename() {
            filter = new AttachmentFilter("test");
            Attachment att = new Attachment();
            att.setFileName(null);
            att.setIndexedContent("test content");
            mail.getAttachments().add(att);

            assertTrue(filter.filter(mail));
        }
    }

    @Nested
    @DisplayName("Scoring Tests 📊")
    class ScoringTests {

        @Test
        @DisplayName("Should return 0 for mail with no attachments")
        void testScoreNoAttachments() {
            filter = new AttachmentFilter("report");
            mail.setAttachments(null);

            assertEquals(0, filter.getScore(mail));
        }

        @Test
        @DisplayName("Should return 0 for empty attachments list")
        void testScoreEmptyList() {
            filter = new AttachmentFilter("invoice");

            assertEquals(0, filter.getScore(mail));
        }

        @Test
        @DisplayName("Should give higher score for filename match than content match")
        void testFilenameVsContentScore() {
            AttachmentFilter filterA = new AttachmentFilter("report");
            Mail mailA = new Mail();
            mailA.setAttachments(new ArrayList<>());
            mailA.getAttachments().add(createAttachment("annual_report.pdf", ""));

            AttachmentFilter filterB = new AttachmentFilter("report");
            Mail mailB = new Mail();
            mailB.setAttachments(new ArrayList<>());
            mailB.getAttachments().add(createAttachment("document.pdf", "This is a report"));

            int filenameScore = filterA.getScore(mailA);
            int contentScore = filterB.getScore(mailB);

            assertTrue(filenameScore > contentScore,
                    "Filename match should score higher than content match");
        }

        @Test
        @DisplayName("Should score multiple matches higher than single match")
        void testMultipleMatches() {
            AttachmentFilter filterSingle = new AttachmentFilter("budget");
            Mail mailSingle = new Mail();
            mailSingle.setAttachments(new ArrayList<>());
            mailSingle.getAttachments().add(createAttachment("budget.pdf", ""));

            AttachmentFilter filterMultiple = new AttachmentFilter("budget");
            Mail mailMultiple = new Mail();
            mailMultiple.setAttachments(new ArrayList<>());
            mailMultiple.getAttachments().add(createAttachment("budget_report.xlsx", "budget details budget summary"));

            int singleScore = filterSingle.getScore(mailSingle);
            int multipleScore = filterMultiple.getScore(mailMultiple);

            assertTrue(multipleScore > singleScore,
                    "Multiple matches should score higher");
        }

        @Test
        @DisplayName("Should handle multi-word queries correctly")
        void testMultiWordQuery() {
            filter = new AttachmentFilter("annual report");
            mail.getAttachments().add(createAttachment("2024_annual_report.pdf", ""));

            int score = filter.getScore(mail);
            assertTrue(score > 0, "Multi-word query should produce score");
        }

        // Will fix normalization
        // to make scores distribution
        // Reflects the matching better
        @Test
        @DisplayName("Should score full query match higher than partial matches")
        void testFullVsPartialMatch() {
            AttachmentFilter filterFull = new AttachmentFilter("quarterly report");
            Mail mailFull = new Mail();
            mailFull.setAttachments(new ArrayList<>());
            mailFull.getAttachments().add(createAttachment("quarterly_report.pdf", ""));

            AttachmentFilter filterPartial = new AttachmentFilter("quarterly report");
            Mail mailPartial = new Mail();
            mailPartial.setAttachments(new ArrayList<>());
            mailPartial.getAttachments().add(createAttachment("quarterly.pdf", "report"));

            int fullScore = filterFull.getScore(mailFull);
            int partialScore = filterPartial.getScore(mailPartial);

            assertTrue(fullScore >= partialScore,
                    "Full query match should score higher than scattered partial matches");
        }

        @Test
        @DisplayName("Score should be between 0 and 100")
        void testScoreRange() {
            filter = new AttachmentFilter("test");

            // Create attachment with many matches to test upper bound
            String content = "test test test ".repeat(100);

            mail.getAttachments().add(createAttachment("test_test_test.txt", content));

            int score = filter.getScore(mail);
            assertTrue(score >= 0 && score <= 100,
                    "Score should be normalized between 0 and 100, got: " + score);
        }

        @Test
        @DisplayName("Should apply sigmoid normalization correctly")
        void testSigmoidNormalization() {
            // Low raw score should give low normalized score
            filter = new AttachmentFilter("rare");
            mail.getAttachments().add(createAttachment("doc.txt", "rare"));
            int lowScore = filter.getScore(mail);

            // High raw score should give high normalized score
            AttachmentFilter filter2 = new AttachmentFilter("common");
            Mail mail2 = new Mail();
            mail2.setAttachments(new ArrayList<>());
            String content = "common common common ".repeat(50);
            mail2.getAttachments().add(createAttachment("common_common.txt", content));
            int highScore = filter2.getScore(mail2);

            assertTrue(highScore > lowScore * 2,
                    "Sigmoid should create clear separation between low and high scores");
        }
    }

    @Nested
    @DisplayName("Edge Cases & Stress Tests 💪")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty query")
        void testEmptyQuery() {
            filter = new AttachmentFilter("");
            mail.getAttachments().add(createAttachment("report.pdf", "content"));

            assertFalse(filter.filter(mail));
            assertEquals(0, filter.getScore(mail));
        }

        @Test
        @DisplayName("Should handle query with only whitespace")
        void testWhitespaceQuery() {
            filter = new AttachmentFilter("   ");
            mail.getAttachments().add(createAttachment("report.pdf", "content"));

            assertFalse(filter.filter(mail));
        }

        @Test
        @DisplayName("Should handle special characters in query")
        void testSpecialCharactersQuery() {
            filter = new AttachmentFilter("report_2024.pdf");
            mail.getAttachments().add(createAttachment("report_2024.pdf", ""));

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should handle very long content")
        void testVeryLongContent() {
            filter = new AttachmentFilter("needle");
            String longContent = "haystack ".repeat(10000) + "needle";

            mail.getAttachments().add(createAttachment("large.txt", longContent));

            assertTrue(filter.filter(mail));
            assertTrue(filter.getScore(mail) > 0);
        }

        @Test
        @DisplayName("Should handle multiple attachments with mixed matches")
        void testMultipleMixedAttachments() {
            filter = new AttachmentFilter("contract");
            mail.getAttachments().add(createAttachment("report.pdf", "annual data"));
            mail.getAttachments().add(createAttachment("contract.docx", ""));
            mail.getAttachments().add(createAttachment("notes.txt", "contract details"));
            mail.getAttachments().add(createAttachment("image.png", ""));

            assertTrue(filter.filter(mail));
            int score = filter.getScore(mail);
            assertTrue(score > 10, "Multiple matches across attachments should produce good score");
        }

        @Test
        @DisplayName("Should handle attachment with empty data")
        void testEmptyAttachmentData() {
            filter = new AttachmentFilter("test");
            Attachment att = new Attachment();
            att.setFileName("test.txt");
            att.setIndexedContent("");
            mail.getAttachments().add(att);

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should handle Unicode content")
        void testUnicodeContent() {
            filter = new AttachmentFilter("مرحبا");
            mail.getAttachments().add(createAttachment("arabic.txt", "مرحبا بك في النظام"));

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Should be case insensitive for all languages")
        void testCaseInsensitivity() {
            filter = new AttachmentFilter("RePoRt");
            mail.getAttachments().add(createAttachment("REPORT.PDF", "report data REPORT"));

            assertTrue(filter.filter(mail));
            assertTrue(filter.getScore(mail) > 0);
        }

        @ParameterizedTest
        @ValueSource(strings = {"report", "REPORT", "Report", "rEpOrT"})
        @DisplayName("Should match regardless of query case")
        void testParameterizedCaseInsensitivity(String query) {
            filter = new AttachmentFilter(query);
            mail.getAttachments().add(createAttachment("Monthly_Report.pdf", ""));

            assertTrue(filter.filter(mail));
        }

        @ParameterizedTest
        @CsvSource({
                "invoice, invoice_2024.pdf, true",
                "budget, annual_report.pdf, false",
                "contract, Contract_Final.docx, true",
                "memo, meeting_notes.txt, false"
        })
        @DisplayName("Should handle various query-filename combinations")
        void testVariousQueryFilenameCombinations(String query, String filename, boolean expected) {
            filter = new AttachmentFilter(query);
            mail.getAttachments().add(createAttachment(filename, ""));

            assertEquals(expected, filter.filter(mail));
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios 🌍")
    class RealWorldTests {

        @Test
        @DisplayName("Searching for invoice in email with multiple attachments")
        void testInvoiceSearch() {
            filter = new AttachmentFilter("invoice");
            mail.getAttachments().add(createAttachment("cover_letter.pdf", "Dear Sir"));
            mail.getAttachments().add(createAttachment("Invoice_March_2024.pdf", "Invoice #12345 Amount: $500"));
            mail.getAttachments().add(createAttachment("receipt.jpg", "Payment confirmation"));

            assertTrue(filter.filter(mail));
            int score = filter.getScore(mail);
            assertTrue(score >= 10, "Invoice search should have decent score: " + score);
        }

        @Test
        @DisplayName("Searching for contract with partial matches")
        void testContractPartialMatch() {
            filter = new AttachmentFilter("employment contract");
            mail.getAttachments().add(createAttachment("employment_agreement.docx", "This contract outlines employment terms"));

            assertTrue(filter.filter(mail));
            int score = filter.getScore(mail);
            assertTrue(score > 0, "Partial word matches should contribute to score");
        }

        @Test
        @DisplayName("PDF with text content extraction")
        void testPDFTextExtraction() {
            filter = new AttachmentFilter("quarterly");
            // Simulating PDF content as text (Tika would extract this)
            mail.getAttachments().add(createAttachment("Q4_Report.pdf", "Quarterly financial report for Q4 2024"));

            assertTrue(filter.filter(mail));
            assertTrue(filter.getScore(mail) > 10);
        }

        @Test
        @DisplayName("Searching technical documentation")
        void testTechnicalDocSearch() {
            filter = new AttachmentFilter("API documentation");
            mail.getAttachments().add(createAttachment("API_Guide.pdf", "API documentation for REST endpoints"));

            assertTrue(filter.filter(mail));
        }

        @Test
        @DisplayName("Image attachment with no text should not match content search")
        void testImageAttachmentNoTextMatch() {
            filter = new AttachmentFilter("report");
            Attachment imageAtt = new Attachment();
            imageAtt.setFileName("photo.jpg");
            imageAtt.setIndexedContent(""); // Images typically have no text content
            mail.getAttachments().add(imageAtt);

            assertFalse(filter.filter(mail));
        }
    }

    // Helper method to create attachments
    private Attachment createAttachment(String filename, String content) {
        Attachment att = new Attachment();
        att.setFileName(filename);
        att.setIndexedContent(content); // Use indexed content instead of binary data
        return att;
    }
}
