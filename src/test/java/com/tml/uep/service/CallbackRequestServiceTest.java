package com.tml.uep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.Channel;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.callbackrequest.CallbackCreationRequest;
import com.tml.uep.model.entity.CallbackRequestEntity;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.repository.CallbackRequestRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.tml.uep.data.CustomerQueryData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class CallbackRequestServiceTest {

    @Autowired
    private CallbackRequestService callbackRequestService;

    @Autowired
    private CallbackRequestRepository repository;
    @Autowired private ObjectMapper mapper;

    @Before
    public void setupAll() {
        repository.deleteAll();
    }

    @Test
    public void shouldCreateCallbackRequestWhenValidCustomerCallbackIsSent() throws Exception {

        //given
        OffsetDateTime startDateTime = OffsetDateTime.now();
        OffsetDateTime endDateTime = OffsetDateTime.now().plusMinutes(120);

        CallbackCreationRequest request =
                new CallbackCreationRequest(startDateTime, endDateTime, MOBILE_NUMBER);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
                        "",
                        Scenario.CALLBACK_REQUEST,
                        "",
                        MessageType.TEXT,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        mapper.writeValueAsString(request),
                        false);

        //when
        Boolean isCreated = callbackRequestService.processMessage(incomingMessage);

        //then
        assertEquals(true, isCreated);
        List<CallbackRequestEntity> callbackRequestEntities = repository.findAll();
        Assert.assertEquals(1, callbackRequestEntities.size());
        CallbackRequestEntity callbackRequestEntity = callbackRequestEntities.get(0);
        Assert.assertEquals(MOBILE_NUMBER, callbackRequestEntity.getMobileNumber());
        Assert.assertEquals(CUSTOMER_ID, callbackRequestEntity.getCustomerId());
        Assert.assertNotNull(callbackRequestEntity.getId());
        Assert.assertEquals(startDateTime.toEpochSecond(), callbackRequestEntity.getStartDateTime().toEpochSecond());
        Assert.assertEquals(endDateTime.toEpochSecond(), callbackRequestEntity.getEndDateTime().toEpochSecond());
        Assert.assertEquals(CallbackRequestStatus.NOT_STARTED, callbackRequestEntity.getCallbackRequestStatus());
    }


    @Test
    public void shouldNotCreateCallbackRequestWhenMobileNumberIsNotPassedAndThrowConstraintViolationException()
            throws Exception {

        //given
        OffsetDateTime startDateTime = OffsetDateTime.now();
        OffsetDateTime endDateTime = OffsetDateTime.now().plusMinutes(120);

        CallbackCreationRequest request =
                new CallbackCreationRequest(startDateTime, endDateTime, null);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
                        "",
                        Scenario.CALLBACK_REQUEST,
                        "",
                        MessageType.TEXT,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        mapper.writeValueAsString(request),
                        false);

        //then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    //when
                    callbackRequestService.processMessage(incomingMessage);                }
        );

        assertEquals("mobileNumber: must not be blank", exception.getMessage());
    }

    @Test
    public void shouldNotCreateCallbackRequestWhenStartDateTimeIsNotPassedAndThrowConstraintViolationException()
            throws Exception {

        //given
        OffsetDateTime endDateTime = OffsetDateTime.now().plusMinutes(120);

        CallbackCreationRequest request =
                new CallbackCreationRequest(null, endDateTime, MOBILE_NUMBER);
        IncomingMessage incomingMessage =
                new IncomingMessage(
                        CUSTOMER_ID,
                        LocalDateTime.now(ZoneId.of("UTC")),
                        MESSAGE_ID,
                        MESSAGE,
                        "",
                        Scenario.CALLBACK_REQUEST,
                        "",
                        MessageType.TEXT,
                        null,
                        null,
                        "",
                        "",
                        Channel.FLEET_EDGE_BOT,
                        null,
                        mapper.writeValueAsString(request),
                        false);

        //then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    //when
                    callbackRequestService.processMessage(incomingMessage);                }
        );

        assertEquals("startDateTime: must not be null", exception.getMessage());
    }
}