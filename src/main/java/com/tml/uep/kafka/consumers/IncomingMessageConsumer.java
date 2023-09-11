package com.tml.uep.kafka.consumers;

import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.service.IncomingChatService;
import com.tml.uep.utils.MaskingUtils;
import com.tml.uep.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IncomingMessageConsumer {

    @Autowired private Utils utils;

    @Autowired private IncomingChatService incomingChatService;

    @KafkaListener(
            topics = "#{'${kafka.incoming-chat-queue-topic}'.split(',')}",
            containerFactory = "incomingKafkaListenerContainerFactory")
    public void consume(@Payload String message) {
        IncomingMessage incomingMessage = utils.toType(message, IncomingMessage.class);
        log.info(
                "Received an Incoming message for customer id: {} with data: {} and scenario: {} and type: {}",
                MaskingUtils.maskString(incomingMessage.getCustomerId()),
                incomingMessage.getData(),
                incomingMessage.getScenario(),
                incomingMessage.getMessageType());

        incomingChatService.processMessage(incomingMessage);

    }
}
