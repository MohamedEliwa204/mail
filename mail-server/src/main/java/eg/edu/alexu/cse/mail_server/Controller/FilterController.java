package eg.edu.alexu.cse.mail_server.Controller;


import eg.edu.alexu.cse.mail_server.Service.FilterService;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import eg.edu.alexu.cse.mail_server.dto.MailFilterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filter")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class FilterController {
    private final FilterService filterService;

    @PostMapping("/and")
    public List<EmailViewDto> getEmailsAnd(@RequestBody MailFilterDTO mailFilterDTO) {
        return filterService.getEmailsAnd(mailFilterDTO);
    }


    @PostMapping("/or")
    public List<EmailViewDto> getEmailsOr(@RequestBody MailFilterDTO mailFilterDTO) {
        return filterService.getEmailsOr(mailFilterDTO);
    }

    @PostMapping("/search")
    public List<EmailViewDto> searchEmails(@RequestBody MailFilterDTO mailFilterDTO) {
        return filterService.getEmailsAnd(mailFilterDTO);
    }
}
