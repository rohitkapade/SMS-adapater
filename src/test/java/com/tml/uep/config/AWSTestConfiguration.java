package com.tml.uep.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;

@TestConfiguration
public class AWSTestConfiguration {
    @MockBean private SimpleMessageListenerContainer simpleMessageListenerContainer;
}
