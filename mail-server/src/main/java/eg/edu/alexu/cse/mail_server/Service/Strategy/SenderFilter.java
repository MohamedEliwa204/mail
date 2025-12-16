package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SenderFilter implements FilterStrategy {
    // For now we will use sender name
    private String senderName ;

    public SenderFilter(String senderName) {
        this.senderName = senderName.trim().toLowerCase();
    }

    public SenderFilter() {
    }

    @Override
    public boolean filter(Mail mail) {
        User sender = Optional.ofNullable(mail.getSenderRel())
                .orElseThrow(() -> new NoSuchElementException("Sender is empty"));

        String fullName = String.join(" ",
                sender.getFirstName(),
                sender.getLastName()
        ).toLowerCase();

        String email = sender.getEmail().toLowerCase();

        String query = senderName.toLowerCase().trim();

        // Check if query matches as a substring in full name
        if (fullName.contains(query) || email.contains(query)) {
            return true;
        }

        // Check if query matches email local part
        String emailLocalPart = email.split("@")[0];
        if (emailLocalPart.contains(query)) {
            return true;
        }

        // Fall back to token-based prefix matching
        String[] senderTokens = (fullName + " " + emailLocalPart).split("[\\s@._+-]+");
        String[] queryTokens = query.split("\\s+");

        for (String q : queryTokens) {
            boolean matched = Arrays.stream(senderTokens)
                    .anyMatch(s -> s.startsWith(q));
            if (!matched) return false;
        }

        return true;
    }

    /**
     * Returns a score based on how closely the mail's sender matches the target senderName.
     * Higher score indicates stronger match.
     */
    public int getScore (Mail mail) {
        User sender = Optional.ofNullable(mail.getSenderRel()).orElseThrow(() ->  new NoSuchElementException("Sender is Empty"));

        String fullName = String.join(" ",
                sender.getFirstName(),
                sender.getLastName()
        ).toLowerCase();

        String email = sender.getEmail().toLowerCase();
        String emailLocalPart = email.split("@")[0];

        // Exact email match - highest score
        if (email.equals(senderName)) {
            return 100;
        }

        // Exact name match
        if (fullName.equals(senderName)) {
            return 90;
        }

        // Exact local part match
        if (emailLocalPart.equals(senderName)) {
            return 80;
        }

        // Email starts with query
        if (email.startsWith(senderName)) {
            return 70;
        }

        // Name starts with query
        if (fullName.startsWith(senderName)) {
            return 60;
        }

        // Email contains query
        if (email.contains(senderName)) {
            return 50;
        }

        // Name contains query
        if (fullName.contains(senderName)) {
            return 40;
        }

        // Token-based prefix matching
        String[] receiverTokens = (fullName + " " + emailLocalPart).split("[\\s@._+-]+");
        String[] queryTokens = senderName.split("\\s+");

        int matchedTokens = 0;
        for (String q : queryTokens) {
            boolean matched = Arrays.stream(receiverTokens)
                    .anyMatch(s -> s.startsWith(q));
            if (matched) matchedTokens++;
        }

        if (matchedTokens == queryTokens.length) {
            return 30; // All tokens matched
        }

        return 0;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName.trim().toLowerCase();
    }
}
