package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import eg.edu.alexu.cse.mail_server.Service.command.DraftCommand;
import eg.edu.alexu.cse.mail_server.Service.command.SendCommand;

import eg.edu.alexu.cse.mail_server.dto.ComposeEmailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {
    private final MailRepository mailRepository;
    private final UserRepository userRepository;
    private final SendCommand sendCommand;
    private final DraftCommand draftCommand;

    public void send(ComposeEmailDTO composeEmailDTO) {
        sendCommand.execute(composeEmailDTO);
    }

    public void draft(ComposeEmailDTO composeEmailDTO) {
        draftCommand.execute(composeEmailDTO);
    }
}
