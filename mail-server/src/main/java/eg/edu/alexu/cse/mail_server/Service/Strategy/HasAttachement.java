package eg.edu.alexu.cse.mail_server.Service.Strategy;

import eg.edu.alexu.cse.mail_server.Entity.Mail;

public class HasAttachement implements FilterStrategy {

    @Override
    public boolean filter(Mail mail) {
        var attach = mail.getAttachments();
        if (attach == null || attach.isEmpty()) return false;
        return true;
    }

    @Override
    public int getScore(Mail mail) {
        return (filter(mail) ? 100 : 0);
    }
}
