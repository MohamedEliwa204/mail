package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ReceiverFilter implements FilterStrategy {

    private String[] receivers;

    public ReceiverFilter(String[] receivers) {
        this.receivers = receivers;
    }

    public String[] getReceivers() {
        return receivers;
    }

    public void setReceivers(String[] receivers) {
        this.receivers = receivers;
    }

    @Override
    public boolean filter(Mail mail) {

        // Using optional to avoid polluting code with null checks
        List<User> mailReceivers = Optional.ofNullable(mail.getReceiverRel())
                .orElseThrow(() -> new NoSuchElementException("Receivers list is empty"));

        if (receivers == null || receivers.length == 0) return false ;

        for (String receiver : receivers)
        {
            String query = receiver.trim().toLowerCase();
            for (User user : mailReceivers) {
                if (matchesReceiver(user,query)) return true;
            }
        }
        return true;
    }


    private boolean matchesReceiver(User receiver, String query) {
        String fullName = String.join(" ",
                receiver.getFirstName(),
                receiver.getLastName()
        ).toLowerCase();

        String email = receiver.getEmail().toLowerCase();

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
        String[] receiverTokens = (fullName + " " + emailLocalPart).split("[\\s@._+-]+");
        String[] queryTokens = query.split("\\s+");

        for (String q : queryTokens) {
            boolean matched = Arrays.stream(receiverTokens)
                    .anyMatch(s -> s.startsWith(q));
            if (!matched) return false;
        }

        return false;
    }

    @Override
    public int getScore(Mail mail) {
        List<User> mailReceivers = Optional.ofNullable(mail.getReceiverRel())
                .orElseThrow(() -> new NoSuchElementException("Receivers list is empty"));

        int maxScore = 0;
        // Currently the max score
        // can be updated for a better score matching
        for (String receiver : receivers) {
            String query = receiver.trim().toLowerCase();
            for (User user : mailReceivers) {
                int score = calculateMatchScore(user, query);
                maxScore = Math.max(maxScore, score);
            }
        }
        return maxScore;
    }

    private int calculateMatchScore(User receiver, String query) {
        String fullName = String.join(" ",
                receiver.getFirstName(),
                receiver.getLastName()
        ).toLowerCase();

        String email = receiver.getEmail().toLowerCase();
        String emailLocalPart = email.split("@")[0];

        // Exact email match - highest score
        if (email.equals(query)) {
            return 100;
        }

        // Exact name match
        if (fullName.equals(query)) {
            return 90;
        }

        // Exact local part match
        if (emailLocalPart.equals(query)) {
            return 80;
        }

        // Email starts with query
        if (email.startsWith(query)) {
            return 70;
        }

        // Name starts with query
        if (fullName.startsWith(query)) {
            return 60;
        }

        // Email contains query
        if (email.contains(query)) {
            return 50;
        }

        // Name contains query
        if (fullName.contains(query)) {
            return 40;
        }

        // Token-based prefix matching
        String[] receiverTokens = (fullName + " " + emailLocalPart).split("[\\s@._+-]+");
        String[] queryTokens = query.split("\\s+");

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
}

