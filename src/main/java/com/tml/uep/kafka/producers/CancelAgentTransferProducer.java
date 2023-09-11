package com.tml.uep.kafka.producers;

import com.tml.uep.model.kafka.CancelAgentTransferMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Component
@Slf4j
public class CancelAgentTransferProducer {

    @Value("#{'${kafka.producer.cancelAgentTransferProducerTopic}'.split(',')}")
    private List<String> producerTopic;

    @Autowired private KafkaTemplate<String, CancelAgentTransferMessage> cancelAgentTransferMessageKafkaTemplate;

    public ListenableFuture<SendResult<String, CancelAgentTransferMessage>> send(
            String key, CancelAgentTransferMessage cancelAgentTransferMessage) {
        log.info("Cancel agent transfer request for customer id={} and channel={}",
                cancelAgentTransferMessage.getCustomerId(), cancelAgentTransferMessage.getChannel());

        ListenableFuture<SendResult<String, CancelAgentTransferMessage>> responseFuture =
                cancelAgentTransferMessageKafkaTemplate.send(producerTopic.get(0), key, cancelAgentTransferMessage);
        responseFuture.addCallback(
                new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Error producing message on kafka, cause: " + ex);
                    }

                    @Override
                    public void onSuccess(SendResult<String, CancelAgentTransferMessage> result) {}
                });
        return responseFuture;
    }
}
