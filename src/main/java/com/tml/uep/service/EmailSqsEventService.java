package com.tml.uep.service;

import com.tml.uep.model.entity.EmailSqsEvent;
import com.tml.uep.repository.EmailSqsEventRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailSqsEventService {
    private EmailSqsEventRepository emailSqsEventRepository;

    @Autowired
    public EmailSqsEventService(EmailSqsEventRepository emailSqsEventRepository) {
        this.emailSqsEventRepository = emailSqsEventRepository;
    }

    public Optional<String> getSequencerByFileName(String fileName) {
        return this.emailSqsEventRepository.findById(fileName).map(EmailSqsEvent::getSequencer);
    }

    public Optional<String> getMd5HashByFileName(String fileName) {
        return this.emailSqsEventRepository
                .findById(fileName)
                .map(EmailSqsEvent::getMd5AttachmentHash);
    }

    public boolean isMatchingMd5AttachmentHashFound(String md5Hash) {
        return this.emailSqsEventRepository.findByMd5AttachmentHash(md5Hash).isPresent();
    }

    public void saveEmailEvent(EmailSqsEvent event) {
        this.emailSqsEventRepository.save(event);
    }
}
