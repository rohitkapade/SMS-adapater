package com.tml.uep.config.kafka;

import com.tml.uep.model.kafka.CancelAgentTransferMessage;
import com.tml.uep.model.kafka.OutboundEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.producer.clientId}")
    private String producerClientId;

    public Map<String, Object> producerConfigurations(String clientIdPrefix) {
        Map<String, Object> configurations = new HashMap<>();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        configurations.put(
                ProducerConfig.CLIENT_ID_CONFIG, producerClientId + "-" + clientIdPrefix);
        configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configurations;
    }

    @Bean(name = "eventKafkaTemplate")
    public KafkaTemplate<String, OutboundEvent> eventKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory("eventKafkaProducer"));
    }

    @Bean(name = "errorEventKafkaTemplate")
    public KafkaTemplate<String, OutboundEvent> errorEventKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory("errorEventKafkaProducer"));
    }

    @Bean(name = "cancelAgentTransferMessageKafkaTemplate")
    public KafkaTemplate<String, CancelAgentTransferMessage> cancelAgentTransferMessageKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigurations("cancelAgentTransfer")));
    }

    @Bean
    public ProducerFactory<String, OutboundEvent> producerFactory(String clientIdPrefix) {
        return new DefaultKafkaProducerFactory<>(producerConfigurations(clientIdPrefix));
    }
}
