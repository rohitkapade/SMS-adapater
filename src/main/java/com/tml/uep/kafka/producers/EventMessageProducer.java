package com.tml.uep.kafka.producers;

import com.tml.uep.model.kafka.OutboundEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class EventMessageProducer {

    @Value("#{'${kafka.producer.eventProducerTopic}'.split(',')}")
    private List<String> producerTopic;

    @Autowired
    @Qualifier("eventKafkaTemplate")
    private KafkaTemplate<String, OutboundEvent> eventKafkaProducer;

    public ListenableFuture<SendResult<String, OutboundEvent>> send(
            String key, OutboundEvent eventMessage) {
        log.info("event message to be produced on kafka topic - {}", eventMessage);
        ListenableFuture<SendResult<String, OutboundEvent>> responseFuture =
                eventKafkaProducer.send(producerTopic.get(0), key, eventMessage);
        responseFuture.addCallback(
                new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Error producing message on kafka, cause: " + ex);
                    }

                    @Override
                    public void onSuccess(SendResult<String, OutboundEvent> result) {}
                });
        return responseFuture;
    }
}
