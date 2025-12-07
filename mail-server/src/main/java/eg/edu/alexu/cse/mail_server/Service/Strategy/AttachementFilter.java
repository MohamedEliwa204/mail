package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Attachment;
import eg.edu.alexu.cse.mail_server.Entity.Mail;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttachementFilter implements FilterStrategy{

    private String query ;

    public AttachementFilter(String query) {
        this.query = query;
    }

    /**
     * Need to know different types of
     * attachments to be able to handle them
     */
    @Override
    public boolean filter(Mail mail) {
        return false ;
    }

    @Override
    public int getScore(Mail mail) {
        return 100 ;
    }
}
