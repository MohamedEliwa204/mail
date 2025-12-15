package eg.edu.alexu.cse.mail_server.Service;

import eg.edu.alexu.cse.mail_server.Entity.Contact;
import eg.edu.alexu.cse.mail_server.Entity.User;
import eg.edu.alexu.cse.mail_server.Repository.ContactRepository;
import eg.edu.alexu.cse.mail_server.Repository.UserRepository;
import eg.edu.alexu.cse.mail_server.dto.ContactDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public void addContact(ContactDTO dto, String email){
        Optional<User> user = userRepository.findByEmail(email);
        Contact contact = Contact.builder()
                            .name(dto.getName())
                            .emails(dto.getEmails())
                            .user(user.orElse(null))
                            .build();

        contactRepository.save(contact);
    }

    
    public void editContact(ContactDTO newDto){

        if(newDto.getId() == null)
            throw new IllegalArgumentException("Contact ID cannot be null");

        Contact contact = contactRepository.findById(newDto.getId())
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + newDto.getId()));

        contact.setName(newDto.getName());
        contact.setEmails(newDto.getEmails());

        contactRepository.save(contact);
    }
    

    public void deleteContact(Long id){
        System.out.println("***************************************");
        System.out.println("DELETED");
        System.out.println("***************************************");
        contactRepository.deleteById(id);
    }

    public List<Contact> getContacts(String email, String sort){
        if(sort.equals("false"))
            return contactRepository.findByUser_EmailOrderByNameDesc(email);
        else if(sort.equals("true"))
            return contactRepository.findByUser_EmailOrderByNameAsc(email);
        else
            return contactRepository.findByUser_Email(email);
    }
}