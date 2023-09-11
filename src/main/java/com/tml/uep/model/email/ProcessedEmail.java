package com.tml.uep.model.email;

import com.tml.uep.model.kafka.OutboundEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProcessedEmail {
    String md5AttachmentHash;
    OutboundEvent outboundEvent;
    String crmTransactionId;
}
