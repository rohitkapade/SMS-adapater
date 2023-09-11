package com.tml.uep.model;

import com.tml.uep.config.AWSTestConfiguration;
import java.time.OffsetDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.Import;

@Import(AWSTestConfiguration.class)
public class ServiceScheduledEventTest {

    @Test
    public void isValidIfAllRequiredFieldsArePresent() {
        ServiceScheduledEvent serviceScheduledEvent =
                new ServiceScheduledEvent(
                        "sr_id",
                        "1234567890",
                        "saFn",
                        "saLn",
                        "1234",
                        OffsetDateTime.now(),
                        "deName",
                        "123");
        Assert.assertTrue(serviceScheduledEvent.isValid());
    }

    @Test
    public void isInValidIfAllRequiredFieldsAreNotPresent() {
        ServiceScheduledEvent serviceScheduledEvent =
                new ServiceScheduledEvent(
                        "sr_id", "", null, null, "1234", OffsetDateTime.now(), "deName", "123");
        Assert.assertFalse(serviceScheduledEvent.isValid());
    }
}
