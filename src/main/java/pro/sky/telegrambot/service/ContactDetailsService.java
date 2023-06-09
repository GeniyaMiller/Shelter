package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.ContactDetails;
import pro.sky.telegrambot.repository.ContactDetailsRepository;

@Service
public class ContactDetailsService {
    private final ContactDetailsRepository contactDetailsRepository;

    public ContactDetailsService(ContactDetailsRepository contactDetailsRepository) {
        this.contactDetailsRepository = contactDetailsRepository;
    }

    public void save(ContactDetails contactDetails) {
        contactDetailsRepository.save(contactDetails);
    }
}
