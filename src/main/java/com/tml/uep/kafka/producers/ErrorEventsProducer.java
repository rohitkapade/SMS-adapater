package com.tml.uep.kafka.producers;

import com.tml.uep.model.kafka.OutboundEvent;
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
public class ErrorEventsProducer {

    @Value("#{'${kafka.producer.errorEventsTopic}'}")
    private String producerTopic;

    @Autowired
    @Qualifier("errorEventKafkaTemplate")
    private KafkaTemplate<String, OutboundEvent> errorEventKafkaTemplate;

    public ErrorEventsProducer() {}

    public ListenableFuture<SendResult<String, OutboundEvent>> send(
            String key, OutboundEvent eventMessage) {
        log.info("error event message to be produced to error topic - {}", eventMessage);
        ListenableFuture<SendResult<String, OutboundEvent>> responseFuture =
                errorEventKafkaTemplate.send(producerTopic, key, eventMessage);
        responseFuture.addCallback(
                new ListenableFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Error producing error event message on kafka, cause: " + ex);
                    }

                    @Override
                    public void onSuccess(SendResult<String, OutboundEvent> result) {}
                });
        return responseFuture;
    }
}
