package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.tika.Tika;


public class AttachmentFilter implements FilterStrategy{

    private String query ;
    private final Tika tika = new Tika();

    // Scoring weights
    private static final int FILENAME_FULL_MATCH = 20;
    private static final int FILENAME_PARTIAL_MATCH = 12;
    private static final int CONTENT_FULL_MATCH = 15;
    private static final int CONTENT_PARTIAL_MATCH = 8;

    // Sigmoid normalization constants
    private static final double MAX_RAW_SCORE = 100; // Expected maximum raw score (inflection point)
    private static final double SIGMOID_K = 0.02; // Controls curve steepness (0.01-0.05 recommended)

    public AttachmentFilter(String query) {
        this.query = query.toLowerCase().trim();
    }

    /**
     * Need to know different types of
     * attachments to be able to handle them
     */
    @Override
    public boolean filter(Mail mail) {
        List<Attachment> attachments = mail.getAttachments();
        if (attachments == null || attachments.isEmpty()) return false ;
        if (query.isEmpty()) return false ;

        String[] queryParts = query.split("[\\s,.;:!?_]+");

        for (Attachment attachment : attachments) {
            if (matchesFileName(attachment, queryParts)) return true;
            if (matchesContent(attachment, queryParts)) return true;
        }

        return false;
    }

    private boolean matchesFileName(Attachment attachment, String[] queryParts) {
        String fileName = attachment.getFileName() ;
        if (fileName == null) return false;
        String fileNameLower = fileName.toLowerCase();

        // Check full query match first (higher priority)
        if (fileNameLower.contains(query)) return true;

        // Check partial matches
        for (String part : queryParts) {
            if (!part.isEmpty() && fileNameLower.contains(part)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesContent(Attachment attachment, String[] queryParts) {
        String content = extractText(attachment);
        if (content.isEmpty()) return false;

        String contentLower = content.toLowerCase();

        // Check full query match first
        if (contentLower.contains(query)) return true;

        // Check partial matches
        for (String part : queryParts) {
            if (!part.isEmpty() && contentLower.contains(part)) {
                return true;
            }
        }

        return false;
    }


        @Override
    public int getScore(Mail mail) {
        int score = 0;
        if (mail.getAttachments() == null || mail.getAttachments().isEmpty()) return 0;
        if (query.isEmpty()) return 0;

        String[] parts = query.toLowerCase().split("[\\s,.;:!?_]+");

        for (Attachment attachment : mail.getAttachments()) {
            String fileName = attachment.getFileName();
            if (fileName != null) {
                String fileNameLower = fileName.toLowerCase();
                // full query match
                int fullMatches = countOccurrences(fileNameLower,query) ;
                score+=fullMatches * FILENAME_FULL_MATCH;
                // partial / word match (if there is full match )
                if (fullMatches == 0)
                    for (String part : parts) score += FILENAME_PARTIAL_MATCH * countOccurrences(fileNameLower, part);
            }

            // extract content
            String content = extractText(attachment).toLowerCase();
            if (!content.isEmpty()) {
                int fullMatches = countOccurrences(content,query) ;
                score += CONTENT_FULL_MATCH*fullMatches;
                // Partial Matching if there is no full matches
                if (fullMatches == 0)
                    for (String part : parts) score += CONTENT_PARTIAL_MATCH* countOccurrences(content, part);
            }
        }

        return normalizeScore(score);
    }

    private String extractText(Attachment att) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(att.getData()) ;
            return tika.parseToString(stream);
        }
        catch (Exception e) {
            return "";
        }
    }

    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }

    /**
     * Normalizes raw score to 0-100 range using sigmoid function
     * Formula: 100 / (1 + e^(-k * (rawScore - maxScore)))
     *
     * This creates an S-curve where:
     * - Very low scores get very low normalized values
     * - Scores around maxScore get ~50 (inflection point)
     * - High scores approach 100
     * - Creates clear separation between irrelevant and relevant matches
     */
    private int normalizeScore(int rawScore) {
        if (rawScore == 0) return 0;

        double normalized = 100.0 / (1 + Math.exp(-SIGMOID_K * (rawScore - MAX_RAW_SCORE)));

        return (int) Math.round(Math.min(normalized, 100.0));
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
