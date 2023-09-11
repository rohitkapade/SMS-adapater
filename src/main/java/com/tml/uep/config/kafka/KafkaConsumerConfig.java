package com.tml.uep.config.kafka;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.clientId}")
    private String clientId;

    @Value("${kafka.consumer.consumer-group}")
    private String consumerGroup;

    @Value("${kafka.consumer.autoResetConfig}")
    private String autoResetConfig;

    public Map<String, Object> consumerConfigs(String clientIdPrefix) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, getClientId() + clientIdPrefix);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup + clientIdPrefix);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.autoResetConfig);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    public ConsumerFactory<String, String> kafkaConsumerFactory(String clientId) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(clientId));
    }

    @Bean(name = "incomingKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory("incoming"));
        return factory;
    }

    public String getClientId() {
        return this.clientId + RandomStringUtils.random(4);
    }
}
