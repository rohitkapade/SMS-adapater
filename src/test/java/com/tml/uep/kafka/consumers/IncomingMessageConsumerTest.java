package com.tml.uep.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.Channel;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.entity.CustomerQuery;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.repository.CustomerQueryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.tml.uep.data.CustomerQueryData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class IncomingMessageConsumerTest {

    @Autowired private IncomingMessageConsumer incomingMessageConsumer;

    @Autowired
    private CustomerQueryRepository customerQueryRepository;
    @Autowired private ObjectMapper mapper;

    @Test
    public void shouldCreateCustomerQueryWhenValidCustomerQueryIsSent() throws Exception {

        //given
        CustomerQuery customerQuery = new CustomerQuery(MOBILE_NUMBER, MOBILE_NUMBER, QUERY_VALUE, 123L,CustomerQueryStatus.NOT_STARTED, null);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        MOBILE_NUMBER,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        "3434",
                        "Tell me about Tiago",
                        "",
                        Scenario.CUSTOMER_QUERY,
                        "",
                        MessageType.TEXT,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        mapper.writeValueAsString(customerQuery),
                        false);

        //when
        incomingMessageConsumer.consume(mapper.writeValueAsString(incomingMessage));

        //then
        List<CustomerQuery> customerQueries = customerQueryRepository.findAll();

        assertEquals(1, customerQueries.size());
        assertEquals(MOBILE_NUMBER, customerQueries.get(0).getCustomerId());
        assertEquals(MOBILE_NUMBER, customerQueries.get(0).getMobileNumber());
        assertEquals(QUERY_VALUE, customerQueries.get(0).getQuery());
        assertEquals(123L,customerQueries.get(0).getImageId());
        assertEquals(CustomerQueryStatus.NOT_STARTED, customerQueries.get(0).getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWhenInvalidIncomingMessageIsSent() throws Exception {

        //given
        String incomingMessage = mapper.writeValueAsString("{\"Test\":\"Test Value\"}");

        //when
        incomingMessageConsumer.consume(incomingMessage);
    }


}