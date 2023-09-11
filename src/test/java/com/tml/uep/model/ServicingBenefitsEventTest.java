package com.tml.uep.model;

import com.tml.uep.config.AWSTestConfiguration;
import java.time.OffsetDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import(AWSTestConfiguration.class)
public class ServicingBenefitsEventTest {
    @Test
    public void isValidIfMobileNumberIsPresent() {
        ServicingBenefitsEvent servicingBenefitsEvent =
                new ServicingBenefitsEvent(
                        "92344234", "fn", "ln", "123", OffsetDateTime.now(), "1233");
        Assert.assertTrue(servicingBenefitsEvent.isValid());
    }

    @Test
    public void isInValidIfMobileNumberIsNotPresent() {
        ServicingBenefitsEvent servicingBenefitsEvent =
                new ServicingBenefitsEvent("", "fn", "ln", "123", OffsetDateTime.now(), "1233");
        Assert.assertFalse(servicingBenefitsEvent.isValid());
    }
}
