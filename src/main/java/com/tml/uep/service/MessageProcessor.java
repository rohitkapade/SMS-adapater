package com.tml.uep.service;

import com.tml.uep.model.Scenario;
import com.tml.uep.model.kafka.IncomingMessage;

public interface MessageProcessor {

    public Scenario getScenario();
    public boolean processMessage(IncomingMessage incomingMessage);
}
