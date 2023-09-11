package com.tml.uep.service.impl;

import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.service.IncomingChatService;
import com.tml.uep.service.MessageProcessor;
import com.tml.uep.service.MessageScenarioFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class IncomingChatServiceImpl implements IncomingChatService {

    @Autowired
    private MessageScenarioFactory messageScenarioFactory;

    @Override
    public boolean processMessage(IncomingMessage incomingMessage) {

        Optional<MessageProcessor> messageProcessor = MessageScenarioFactory.getMessageProcessor(incomingMessage.getScenario());
        return messageProcessor.map(mp -> mp.processMessage(incomingMessage)).orElseGet(() -> {log.info("Skipping, Scenario not found for Incoming Message: {}", incomingMessage);return false;});
    }
}
