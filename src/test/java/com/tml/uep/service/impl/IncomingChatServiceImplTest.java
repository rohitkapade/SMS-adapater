package com.tml.uep.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractFuture;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.util.concurrent.ListenableFuture;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.kafka.producers.CancelAgentTransferProducer;
import com.tml.uep.model.Channel;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.entity.CustomerQuery;
import com.tml.uep.model.kafka.CancelAgentTransferMessage;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.repository.CustomerQueryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.tml.uep.data.CustomerQueryData.MOBILE_NUMBER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class IncomingChatServiceImplTest {

    @Autowired
    private IncomingChatServiceImpl incomingChatService;
    @Autowired private ObjectMapper mapper;
    @Autowired private CustomerQueryRepository customerQueryRepository;

    @MockBean private CancelAgentTransferProducer cancelAgentTransferProducer;


    @Before
    public void setupAll() {
        customerQueryRepository.deleteAll();
    }

    @Test
    public void shouldSkipMessageWhenUnknownScenarioIsProvided(){

        //given
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        MOBILE_NUMBER,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        "3434",
                        "Tell me about Tiago",
                        "",
                        null,
                        "",
                        MessageType.CUSTOM,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        "",
                        false);

        //when
        Boolean isProcessed = incomingChatService.processMessage(incomingMessage);

        //then
        assertFalse(isProcessed);
    }

    @Test
    public void shouldProcessMessageSuccessfullyWhenKnownScenarioIsProvided(){

        //given
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        MOBILE_NUMBER,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        "3434",
                        "Tell me about Tiago",
                        "",
                        Scenario.CUSTOMER_QUERY,
                        "",
                        MessageType.CUSTOM,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        "{\"query\": \"Tell me about Tiago\",\"imageUrl\": \"test.com\",\"mobileNumber\": \"8065578978\"}",
                        false);

        //when
        Boolean isProcessed = incomingChatService.processMessage(incomingMessage);

        //then
        List<CustomerQuery> customerQueries = customerQueryRepository.findAll();
        assertEquals(1, customerQueries.size());
        assertEquals("Tell me about Tiago", customerQueries.get(0).getQuery());
        assertTrue(isProcessed);
    }

    @Test
    public void shouldSendCancelAgentTransferMessageWhenCancelAgentTransferScenarioIsProvided(){

        //given
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        MOBILE_NUMBER,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        "3434",
                        "",
                        "",
                        Scenario.CANCEL_AGENT_TRANSFER,
                        "",
                        MessageType.TEXT,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        "",
                        false);

        //when
        Boolean isProcessed = incomingChatService.processMessage(incomingMessage);

        //then
        assertTrue(isProcessed);
        verify(cancelAgentTransferProducer, times(1)).send(any(String.class), any(CancelAgentTransferMessage.class));

    }
}