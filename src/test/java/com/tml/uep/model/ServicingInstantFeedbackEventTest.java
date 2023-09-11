package com.tml.uep.model;

import com.tml.uep.config.AWSTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class ServicingInstantFeedbackEventTest {

    @Test
    public void isValidIfMobileNumberIsPresent() {
        ServicingInstantFeedbackEvent servicingInstantFeedbackEvent =
                new ServicingInstantFeedbackEvent("1234566123", "123123", "regNo", "dealer name");
        Assert.assertTrue(servicingInstantFeedbackEvent.isValid());
    }

    @Test
    public void isInValidIfMobileNumberIsNotPresent() {
        ServicingInstantFeedbackEvent servicingInstantFeedbackEvent =
                new ServicingInstantFeedbackEvent("", "123123", "regNo", "dealer name");
        Assert.assertFalse(servicingInstantFeedbackEvent.isValid());
    }
}
