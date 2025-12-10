package eg.edu.alexu.cse.mail_server.Service.Strategy;

import java.util.Optional;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import lombok.AllArgsConstructor;

// Also Or search if any words matches return true
public class BodyFilter implements FilterStrategy {

    private String body ;
    public BodyFilter() {
    }

    public BodyFilter(String body) {
        this.body = body.toLowerCase().trim();
    }

    @Override
    public boolean filter(Mail mail) {
        // Since the body field can be null
        // the filter supports empty bodies
        String mailBody = Optional.ofNullable(mail.getBody()).orElse("").toLowerCase() ;

        String[] words = body.toLowerCase().split("[\\s,.;:!?]+");

        for (String word : words) if (mailBody.contains(word)) return true;
        return false;
    }

    /**
     * Returns a score based on how closely the mail's subject matches the query.
     * Exact match: 100
     * Partial word matches: 10 points each (max 90)
     * Substring matches: 5 points each (max 50)
     */

    public int getScore(Mail mail) {
        String subject = Optional.ofNullable(mail.getBody()).
                orElse("").
                toLowerCase();
        if (subject.equals(body)) return 100 ;

        // Splitting the query into words
        // any punctuation is valid
        String[] words = body.toLowerCase().split("[\\s,.;:!?]+") ;

        // Calculating the number of exact partial matches
        int partialMatches = 0 ;
        for (String word : words) if (subject.contains(word)) partialMatches++ ;

        if (partialMatches > 0) return Math.min(partialMatches*10 , 90) ;

        // Calculating the number of partial words matches
        int wordPartialMatches = 0 ;
        for (String word : words) {
            for (int i = 1 ; i < word.length() ; i++ ){
                String sub = word.substring(0, i);
                if (subject.contains(sub)) wordPartialMatches++ ;
            }
        };
        if (wordPartialMatches > 0) return Math.min(wordPartialMatches*5 , 50);

        return 0;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body.toLowerCase().trim();
    }
}
