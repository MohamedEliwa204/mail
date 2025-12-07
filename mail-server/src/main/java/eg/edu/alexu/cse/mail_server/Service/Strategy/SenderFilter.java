package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Optional;

public class SenderFilter implements FilterStrategy {
    // For now we will use sender name
    private String senderName ;
    private int score ;

    public SenderFilter(String senderName) {
        this.senderName = senderName;
    }

    public SenderFilter() {
    }

    // The method checks all possible combination for name
    // returns true if any of them matches
    @Override
    public boolean filter(Mail mail) {
        // Using optional to avoid polluting code with null checks
        User sender = Optional.ofNullable(mail.getSenderRel()).orElseThrow(() ->  new NoSuchElementException("Sender is Empty"));

        String firstName = mail.getSenderRel().getFirstName();
        String lastName = mail.getSenderRel().getLastName();

        String fullName = firstName + " " + lastName;
        String fullNameNoSpace = firstName+lastName;

        String fullNameRev = lastName+" "+firstName;
        String fullNameRevNoSpace = lastName+firstName;

        return firstName.equalsIgnoreCase(senderName) ||
                lastName.equalsIgnoreCase(senderName) ||
                fullName.equalsIgnoreCase(senderName) ||
                fullNameNoSpace.equalsIgnoreCase(senderName) ||
                fullNameRev.equalsIgnoreCase(senderName) ||
                fullNameRevNoSpace.equalsIgnoreCase(senderName);

    }

    /**
     * Returns a score based on how closely the mail's sender matches the target senderName.
     * Higher score indicates stronger match.
     */
    public int getScore (Mail mail) {
        User sender = Optional.ofNullable(mail.getSenderRel()).orElseThrow(() ->  new NoSuchElementException("Sender is Empty"));

        String firstName = sender.getFirstName();
        String lastName = sender.getLastName();

        String fullName = firstName + " " + lastName;
        String fullNameNoSpace = firstName+lastName;

        String fullNameRev = lastName+" "+firstName;
        String fullNameRevNoSpace = lastName+firstName;

        if (fullName.equalsIgnoreCase(senderName)) return 100 ;
        if (fullNameNoSpace.equalsIgnoreCase(senderName)) return 90 ;
        if (firstName.equalsIgnoreCase(senderName)) return 80 ;
        if (lastName.equalsIgnoreCase(senderName)) return 70 ;
        if (fullNameRev.equalsIgnoreCase(senderName)) return 60 ;
        if (fullNameRevNoSpace.equalsIgnoreCase(senderName)) return 50 ;

        return 0 ;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
        this.score = 0 ;
    }
}
