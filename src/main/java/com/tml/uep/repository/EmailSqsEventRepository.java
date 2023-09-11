package com.tml.uep.repository;

import com.tml.uep.model.entity.EmailSqsEvent;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSqsEventRepository extends CrudRepository<EmailSqsEvent, String> {

    Optional<EmailSqsEvent> findByMd5AttachmentHash(String md5AttachmentHash);
}
