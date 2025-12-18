package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.tika.Tika;


public class AttachmentFilter implements FilterStrategy{

    private final String query;
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
        if (mail.getAttachments() == null || mail.getAttachments().isEmpty() || query.isEmpty()) {
            return 0;
        }

        String[] parts = query.toLowerCase().split("[\\s,.;:!?_]+");
        int score = 0;

        for (Attachment attachment : mail.getAttachments()) {
            score += scoreFileName(attachment, parts);
            score += scoreContent(attachment, parts);
        }

        return normalizeScore(score);
    }

    private int scoreFileName(Attachment attachment, String[] queryParts) {
        String fileName = attachment.getFileName();
        if (fileName == null) return 0;

        String fileNameLower = fileName.toLowerCase();
        int fullMatches = countOccurrences(fileNameLower, query);
        
        if (fullMatches > 0) {
            return fullMatches * FILENAME_FULL_MATCH;
        }
        
        int partialScore = 0;
        for (String part : queryParts) {
            partialScore += FILENAME_PARTIAL_MATCH * countOccurrences(fileNameLower, part);
        }
        return partialScore;
    }

    private int scoreContent(Attachment attachment, String[] queryParts) {
        String content = extractText(attachment).toLowerCase();
        if (content.isEmpty()) return 0;

        int fullMatches = countOccurrences(content, query);
        
        if (fullMatches > 0) {
            return fullMatches * CONTENT_FULL_MATCH;
        }
        
        int partialScore = 0;
        for (String part : queryParts) {
            partialScore += CONTENT_PARTIAL_MATCH * countOccurrences(content, part);
        }
        return partialScore;
    }

    private String extractText(Attachment att) {
        // 1. FAST PATH: Return pre-calculated text from DB
        if (att.getIndexedContent() != null && !att.getIndexedContent().isEmpty()) {
            return att.getIndexedContent();
        }

        // 2. SLOW PATH: Parse file (Only use this for debugging or recovery)
        Path path = Paths.get(att.getFilePath());
        if (!Files.exists(path)) return "";

        // Use try-with-resources to ensure the file closes
        try (java.io.InputStream stream = Files.newInputStream(path)) {
            // Tika parses the stream directly. Much more memory efficient.
            return tika.parseToString(stream);
        } catch (Exception e) {
            System.err.println("Error parsing file: " + att.getFileName());
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
}
