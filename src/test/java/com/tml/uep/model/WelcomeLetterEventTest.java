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
public class WelcomeLetterEventTest {
    @Test
    public void isValidIfMobileNumberAndAllMandatoryFieldsArePresent() {
        WelcomeLetterEvent welcomeLetterEvent =
                new WelcomeLetterEvent(
                        "9012313234",
                        "fn",
                        "ln",
                        "vrN",
                        OffsetDateTime.now(),
                        "12344",
                        "vn",
                        "add1",
                        "add2",
                        "city",
                        "state",
                        "India",
                        "dn",
                        "dc");

        Assert.assertTrue(welcomeLetterEvent.isValid());
    }

    @Test
    public void isInValidIfMobileNumberIsNotPresent() {
        WelcomeLetterEvent welcomeLetterEvent =
                new WelcomeLetterEvent("", "fn", "ln", "vrN", OffsetDateTime.now(), "12344");
        Assert.assertFalse(welcomeLetterEvent.isValid());
    }
}
