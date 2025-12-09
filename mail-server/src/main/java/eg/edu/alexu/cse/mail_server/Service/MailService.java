package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Service.command.DraftCommand;
import eg.edu.alexu.cse.mail_server.Service.command.GetMailCommand;
import eg.edu.alexu.cse.mail_server.Service.command.SendCommand;
import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MailService {
    private final SendCommand sendCommand;
    private final DraftCommand draftCommand;
    private final GetMailCommand getMailCommand;

    public void send(ComposeEmailDTO composeEmailDTO) {
        sendCommand.execute(composeEmailDTO);
    }

    public void draft(ComposeEmailDTO composeEmailDTO) {
        draftCommand.execute(composeEmailDTO);
    }

    /**
     * Get mail with all attachments including file data
     *
     * @param mailId the ID of the mail
     * @return EmailViewDto with attachments containing file data as Base64
     * @throws IOException if file reading fails
     */
    public EmailViewDto getMailWithAttachments(Long mailId) throws IOException {
        return getMailCommand.execute(mailId);
    }
}
