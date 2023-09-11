package com.tml.uep.service;

import com.tml.uep.model.kafka.IncomingMessage;

public interface IncomingChatService {
    boolean processMessage(IncomingMessage incomingMessage);
}
