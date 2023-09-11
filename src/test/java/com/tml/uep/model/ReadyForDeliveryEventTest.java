package com.tml.uep.model;

import com.tml.uep.config.AWSTestConfiguration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class ReadyForDeliveryEventTest {

    @Test
    public void isValidIfAllRequiredFieldsArePresent() {
        ReadyForDeliveryEvent readyForDeliveryEvent =
                new ReadyForDeliveryEvent(
                        "1234574823",
                        "first name",
                        "last name",
                        "1234",
                        OffsetDateTime.now(),
                        "saFN",
                        "saLN",
                        "1234958504",
                        UUID.randomUUID().toString());
        Assert.assertTrue(readyForDeliveryEvent.isValid());
    }

    @Test
    public void isInValidIfAllRequiredFieldsAreNotPresent() {
        ReadyForDeliveryEvent readyForDeliveryEvent =
                new ReadyForDeliveryEvent(
                        "1234574823",
                        "first name",
                        "last name",
                        "",
                        OffsetDateTime.now(),
                        "saFN",
                        "saLN",
                        "1234958504",
                        UUID.randomUUID().toString());
        Assert.assertFalse(readyForDeliveryEvent.isValid());
    }
}
