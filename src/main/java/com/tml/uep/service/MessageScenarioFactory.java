package com.tml.uep.service;


import com.tml.uep.model.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageScenarioFactory {

    private static Map<Scenario, MessageProcessor> messageProcessorMap;

    @Autowired
    private MessageScenarioFactory(List<MessageProcessor> messageProcessors){

        messageProcessorMap =  messageProcessors.stream().collect(Collectors.toUnmodifiableMap(MessageProcessor::getScenario, Function.identity()));
    }

    public static Optional<MessageProcessor> getMessageProcessor(Scenario scenario){
        return scenario!=null?Optional.ofNullable(messageProcessorMap.get(scenario)):Optional.empty();
    }
}
