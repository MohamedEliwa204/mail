package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SenderFilter implements FilterStrategy {
    // For now we will use sender name
    private String[] senderNames ;

    private UserRepository repo ;

    public SenderFilter(String[] senderNames) {
        this.senderNames = senderNames;
    }

    public SenderFilter(UserRepository repo) {
        this.repo = repo;
    }



    public SenderFilter() {
    }

    @Override
    public boolean filter(Mail mail) {
        Optional<User> senderOpt = repo.findByEmail(mail.getSender()) ;
        if (senderOpt.isEmpty()) throw new NoSuchElementException("sender not found");
        User sender = senderOpt.get();

        for (String senderName : senderNames) {


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
        }

        return false;
    }

    /**
     * Returns a score based on how closely the mail's sender matches the target senderName.
     * Higher score indicates stronger match.
     */
    @Override
    public int getScore(Mail mail) {
        int maxScore = 0;
        Optional<User> senderOpt = repo.findByEmail(mail.getSender()) ;
        if (senderOpt.isEmpty()) throw new NoSuchElementException("sender not found");
        User sender = senderOpt.get();
        for (String queryName : senderNames) {
            String query = queryName.trim().toLowerCase();
                int score = calculateMatchScore(sender, query);
                maxScore = Math.max(maxScore, score);
        }

        return maxScore;
    }

    private int calculateMatchScore(User user, String query) {
        String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
        String email = user.getEmail().toLowerCase();
        String emailLocalPart = email.split("@")[0];

        if (email.equals(query)) return 100;
        if (fullName.equals(query)) return 90;
        if (emailLocalPart.equals(query)) return 80;
        if (email.startsWith(query)) return 70;
        if (fullName.startsWith(query)) return 60;
        if (email.contains(query)) return 50;
        if (fullName.contains(query)) return 40;

        String[] userTokens = (fullName + " " + emailLocalPart).split("[\\s@._+-]+");
        String[] queryTokens = query.split("\\s+");
        int matchedTokens = 0;

        for (String q : queryTokens) {
            boolean matched = Arrays.stream(userTokens).anyMatch(s -> s.startsWith(q));
            if (matched) matchedTokens++;
        }

        return matchedTokens == queryTokens.length ? 30 : 0;
    }

    public String[] getSenderNames() {
        return senderNames;
    }

    public void setSenderNames(List<String> senderNames) {
        this.senderNames = (String[]) senderNames.toArray();
    }
    public void setSenderNames(String[] senderNames) {
        this.senderNames = senderNames;
    }
}
