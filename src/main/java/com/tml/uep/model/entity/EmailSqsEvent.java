package com.tml.uep.model.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "uep_email_sqs_event",
        indexes = {@Index(name = "md5Index", columnList = "md5AttachmentHash")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailSqsEvent {
    @Id private String fileName;
    private String sequencer;
    private String eventTime;
    private String md5AttachmentHash;
    private String crmTransactionNumber;
}
