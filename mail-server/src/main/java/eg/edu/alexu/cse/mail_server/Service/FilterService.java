package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Entity.Mail;
import eg.edu.alexu.cse.mail_server.Repository.MailRepository;
import eg.edu.alexu.cse.mail_server.Service.Decorator.AndDecorator;
import eg.edu.alexu.cse.mail_server.Service.Decorator.OrDecorator;
import eg.edu.alexu.cse.mail_server.Service.Strategy.*;
import eg.edu.alexu.cse.mail_server.dto.EmailViewDto;
import eg.edu.alexu.cse.mail_server.dto.MailFilterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represent filter service
 * it supports search with multiple attributes
 * via and , or (simple and / or for now)
 * it takes MailFilter DTO and based on non-null
 * fields it chooses a group of filters to apply
 * it returns EmailView DTO
 *
 */
@Service
public class FilterService {
    private final MailRepository mailRepository;
    private final HashMap<String , FilterStrategy> filters = new HashMap<>();
    private final MailFilter mailFilter;

    @Autowired
    public FilterService(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
        this.mailFilter = new MailFilter() ;

        FilterStrategy senderFilter = new SenderFilter();
        FilterStrategy SubjectFilter = new SubjectFilter();
        FilterStrategy priorityFilter = new PriorityFilter();
        FilterStrategy exactDateFilter = new ExactDateFilter();
        FilterStrategy beforeDateFilter = new BeforeDateFilter() ;
        FilterStrategy afterDateFilter = new AfterDataFilter() ;
        FilterStrategy bodyFilter = new BodyFilter() ;
        FilterStrategy isReadFilter = new IsReadFilter() ;

        filters.put("sender", senderFilter);
        filters.put("subject", SubjectFilter);
        filters.put("priority", priorityFilter);
        filters.put("exactDate", exactDateFilter);
        filters.put("beforeDate", beforeDateFilter);
        filters.put("afterDate", afterDateFilter);
        filters.put("body", bodyFilter);
        filters.put("isRead", isReadFilter);

    }

    // Currently the method returns all emails
    // and email field will be added to get the
    // emails related to the user requested the filter
    public List<EmailViewDto> getEmailsAnd(MailFilterDTO dto) {
        List<Mail> mails = mailRepository.findAll() ;
        List<EmailViewDto> emails = new ArrayList<>();
        List<FilterStrategy> activeFilters = new ArrayList<>();

        // Refactor : Move these into helper method
        if (dto.getSender().isPresent()) {
            SenderFilter senderFilter = (SenderFilter) filters.get("sender");
            senderFilter.setSenderName(dto.getSender().get());
            activeFilters.add(senderFilter);
        }
        if (dto.getSubject().isPresent()) {
            SubjectFilter subjectFilter = (SubjectFilter) filters.get("subject");
            subjectFilter.setQuery(dto.getSubject().get());
            activeFilters.add(subjectFilter);
        }
        if (dto.getBody().isPresent()) {
            BodyFilter bodyFilter = (BodyFilter) filters.get("body");
            bodyFilter.setBody(dto.getBody().get());
            activeFilters.add(bodyFilter);
        }
        if (dto.getAfterDate().isPresent()) {
            AfterDataFilter afterDataFilter = (AfterDataFilter) filters.get("afterDate");
            afterDataFilter.setDate(dto.getAfterDate().get());
            activeFilters.add(afterDataFilter);
        }
        if (dto.getBeforeDate().isPresent()) {
            BeforeDateFilter beforeDateFilter = (BeforeDateFilter) filters.get("beforeDate");
            beforeDateFilter.setDate(dto.getBeforeDate().get());
            activeFilters.add(beforeDateFilter);
        }
        if (dto.getExactDate().isPresent()) {
            ExactDateFilter exactDateFilter = (ExactDateFilter) filters.get("beforeDate");
            exactDateFilter.setDate(dto.getBeforeDate().get());
            activeFilters.add(exactDateFilter);
        }
        if (dto.getPriority().isPresent()) {
            PriorityFilter priorityFilter = (PriorityFilter) filters.get("priority");
            priorityFilter.setPriority(dto.getPriority().get());
            activeFilters.add(priorityFilter);
        }
        if (dto.getIsRead().isPresent()) {
            IsReadFilter isReadFilter = (IsReadFilter) filters.get("isRead");
            isReadFilter.setRead(dto.getIsRead().get());
            activeFilters.add(isReadFilter);
        }
//        if (dto.getAttachmentContent() != null) {
//            AttachmentContentFilter attachmentFilter = (AttachmentContentFilter) filters.get("attachment");
//            attachmentFilter.setContent(dto.getAttachmentContent());
//            activeFilters.add(attachmentFilter);
//        }
        if (activeFilters.isEmpty()) throw new IllegalArgumentException("Invalid filters");

        FilterStrategy filter = activeFilters.getFirst() ;

        for (int i = 1 ; i < activeFilters.size() ; i++) filter = new AndDecorator(filter , activeFilters.get(i)) ;

        mailFilter.setFilterStrategy(filter);
        return convertToDTO(mailFilter.getEmails(mails)) ;


    }



    public List<EmailViewDto> getEmailsOr(MailFilterDTO dto) {
        List<Mail> mails = mailRepository.findAll();
        List<FilterStrategy> activeFilters = new ArrayList<>();

        if (dto.getSender().isPresent()) {
            SenderFilter senderFilter = (SenderFilter) filters.get("sender");
            senderFilter.setSenderName(dto.getSender().get());
            activeFilters.add(senderFilter);
        }
        if (dto.getSubject().isPresent()) {
            SubjectFilter subjectFilter = (SubjectFilter) filters.get("subject");
            subjectFilter.setQuery(dto.getSubject().get());
            activeFilters.add(subjectFilter);
        }
        if (dto.getBody().isPresent()) {
            BodyFilter bodyFilter = (BodyFilter) filters.get("body");
            bodyFilter.setBody(dto.getBody().get());
            activeFilters.add(bodyFilter);
        }
        if (dto.getAfterDate().isPresent()) {
            AfterDataFilter afterDataFilter = (AfterDataFilter) filters.get("afterDate");
            afterDataFilter.setDate(dto.getAfterDate().get());
            activeFilters.add(afterDataFilter);
        }
        if (dto.getBeforeDate().isPresent()) {
            BeforeDateFilter beforeDateFilter = (BeforeDateFilter) filters.get("beforeDate");
            beforeDateFilter.setDate(dto.getBeforeDate().get());
            activeFilters.add(beforeDateFilter);
        }
        if (dto.getExactDate().isPresent()) {
            ExactDateFilter exactDateFilter = (ExactDateFilter) filters.get("beforeDate");
            exactDateFilter.setDate(dto.getBeforeDate().get());
            activeFilters.add(exactDateFilter);
        }
        if (dto.getPriority().isPresent()) {
            PriorityFilter priorityFilter = (PriorityFilter) filters.get("priority");
            priorityFilter.setPriority(dto.getPriority().get());
            activeFilters.add(priorityFilter);
        }
        if (dto.getIsRead().isPresent()) {
            IsReadFilter isReadFilter = (IsReadFilter) filters.get("isRead");
            isReadFilter.setRead(dto.getIsRead().get());
            activeFilters.add(isReadFilter);
        }
//        if (dto.getAttachmentContent() != null) {
//            AttachmentContentFilter attachmentFilter = (AttachmentContentFilter) filters.get("attachment");
//            attachmentFilter.setContent(dto.getAttachmentContent());
//            activeFilters.add(attachmentFilter);
//        }

        if (activeFilters.isEmpty()) return convertToDTO(mails);

        FilterStrategy filter = activeFilters.getFirst();
        for (int i = 1; i < activeFilters.size(); i++) {
            filter = new OrDecorator(filter, activeFilters.get(i));
        }

        mailFilter.setFilterStrategy(filter);
        List<Mail> filteredMails = mailFilter.getEmails(mails);

        return convertToDTO(filteredMails);
    }

    private EmailViewDto toDTO(Mail mail) {
        EmailViewDto dto = new EmailViewDto();
        dto.setId(mail.getMailId());
        dto.setSender(mail.getSenderRel().getFirstName() + " " + mail.getSenderRel().getLastName());
        dto.setSubject(mail.getSubject());
        dto.setBody(mail.getBody());
        dto.setTimestamp(mail.getTimestamp());
        dto.setRead(mail.isRead());
        return dto;
    }

    private List<EmailViewDto> convertToDTO(List<Mail> mails) {
        List<EmailViewDto> dtos = new ArrayList<>();
        for (Mail mail : mails) {
            dtos.add(toDTO(mail));
        }
        return dtos;
    }


}
