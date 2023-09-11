package com.tml.uep.service.impl;

import com.tml.uep.kafka.producers.CancelAgentTransferProducer;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.customerquery.CustomerQueryRequest;
import com.tml.uep.model.kafka.CancelAgentTransferMessage;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.service.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CancelAgentTransferServiceImpl implements MessageProcessor {

    @Autowired
    private CancelAgentTransferProducer cancelAgentTransferProducer;
    @Override
    public Scenario getScenario() {
        return Scenario.CANCEL_AGENT_TRANSFER;
    }

    @Override
    public boolean processMessage(IncomingMessage incomingMessage) {

        CancelAgentTransferMessage cancelAgentTransferMessage = new CancelAgentTransferMessage(incomingMessage.getCustomerId(), incomingMessage.getChannel());
        cancelAgentTransferProducer.send(cancelAgentTransferMessage.getCustomerId(), cancelAgentTransferMessage);
        return true;
    }
}
