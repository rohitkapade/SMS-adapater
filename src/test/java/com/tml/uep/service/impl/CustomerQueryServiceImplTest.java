package com.tml.uep.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.Channel;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.customerquery.CustomerQueryRequest;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.entity.CustomerQuery;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.repository.CustomerQueryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.tml.uep.data.CustomerQueryData.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class CustomerQueryServiceImplTest {

    @Autowired
    private CustomerQueryServiceImpl customerQueryService;

    @Autowired private ObjectMapper mapper;

    @Autowired private CustomerQueryRepository customerQueryRepository;

    @Before
    public void setupAll() {
        customerQueryRepository.deleteAll();
    }

    @Test
    public void shouldCreateCustomerQueryWhenValidCustomerQueryIsSent() throws Exception {

        //given
        CustomerQueryRequest customerQuery = new CustomerQueryRequest(MOBILE_NUMBER, QUERY_VALUE, 123L);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
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
        Boolean isCreated = customerQueryService.processMessage(incomingMessage);

        //then
        assertEquals(true, isCreated);
        List<CustomerQuery> customerQueries = customerQueryRepository.findAll();

        assertEquals(1, customerQueries.size());
        assertEquals(CUSTOMER_ID, customerQueries.get(0).getCustomerId());
        assertEquals(MOBILE_NUMBER, customerQueries.get(0).getMobileNumber());
        assertEquals(QUERY_VALUE, customerQueries.get(0).getQuery());
        assertEquals(123L, customerQueries.get(0).getImageId());
        assertEquals(CustomerQueryStatus.NOT_STARTED, customerQueries.get(0).getStatus());

    }
    @Test
    public void shouldThrowCustomerIdCannotBeNullErrorWhenCustomerIdIsNotSent() throws Exception {

        //given
        CustomerQueryRequest customerQuery = new CustomerQueryRequest(MOBILE_NUMBER, QUERY_VALUE, 123L);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        null,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
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


        //then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    //when
                    customerQueryService.processMessage(incomingMessage);
                }
        );

        assertEquals(CUSTOMER_ID_NOT_NULL, exception.getMessage());
    }

    @Test
    public void shouldThrowMobileNoCannotBeNullErrorWhenMobileNoIsNotSent() throws Exception {

        //given
        CustomerQueryRequest customerQuery = new CustomerQueryRequest(null, QUERY_VALUE, 123L);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
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


        //then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    //when
                    customerQueryService.processMessage(incomingMessage);
                }
        );

        assertEquals(MOBILE_NUMBER_NOT_NULL, exception.getMessage());
    }

    @Test
    public void shouldThrowQueryCannotBeNullErrorWhenQueryIsNotSent() throws Exception {

        //given
        CustomerQueryRequest customerQuery = new CustomerQueryRequest(MOBILE_NUMBER, null, 123L);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
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


        //then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    //when
                    customerQueryService.processMessage(incomingMessage);
                }
        );

        assertEquals(QUERY_NOT_NULL, exception.getMessage());
    }

    @Test
    public void shouldCreateCustomerQueryWhenImageIsNotSent() throws Exception {

        //given
        CustomerQueryRequest customerQuery = new CustomerQueryRequest(MOBILE_NUMBER, QUERY_VALUE, null);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
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
        Boolean isCreated = customerQueryService.processMessage(incomingMessage);

        //then
        assertEquals(true, isCreated);
        List<CustomerQuery> customerQueries = customerQueryRepository.findAll();

        assertEquals(1, customerQueries.size());
        assertEquals(CUSTOMER_ID, customerQueries.get(0).getCustomerId());
        assertEquals(MOBILE_NUMBER, customerQueries.get(0).getMobileNumber());
        assertEquals(QUERY_VALUE, customerQueries.get(0).getQuery());
        assertEquals(null, customerQueries.get(0).getImageId());
        assertEquals(CustomerQueryStatus.NOT_STARTED, customerQueries.get(0).getStatus());

    }
}