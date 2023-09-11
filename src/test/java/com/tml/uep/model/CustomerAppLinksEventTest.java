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
public class CustomerAppLinksEventTest {

    @Test
    public void isValidIfMobileNumberIsPresent() {
        CustomerAppLinksEvent customerAppLinksEvent =
                new CustomerAppLinksEvent(
                        "1234567890",
                        "test",
                        "last name",
                        "123",
                        OffsetDateTime.now(),
                        UUID.randomUUID().toString());
        Assert.assertTrue(customerAppLinksEvent.isValid());
    }

    @Test
    public void isInValidIfMobileNumberIsNotPresent() {
        CustomerAppLinksEvent customerAppLinksEvent =
                new CustomerAppLinksEvent(
                        null,
                        "test",
                        "last name",
                        "123",
                        OffsetDateTime.now(),
                        UUID.randomUUID().toString());
        Assert.assertFalse(customerAppLinksEvent.isValid());
    }
}
