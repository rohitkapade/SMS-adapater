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
public class WorkshopTourVideoEventTest {
    @Test
    public void isValidIfMobileNumberIsPresent() {
        WorkshopTourVideoEvent workshopTourVideoEvent =
                new WorkshopTourVideoEvent(
                        "123123134", "fn", "ln", "1234", OffsetDateTime.now(), "12313");
        Assert.assertTrue(workshopTourVideoEvent.isValid());
    }

    @Test
    public void isInValidIfMobileNumberIsNotPresent() {
        WorkshopTourVideoEvent workshopTourVideoEvent =
                new WorkshopTourVideoEvent("", "fn", "ln", "1234", OffsetDateTime.now(), "12313");
        Assert.assertFalse(workshopTourVideoEvent.isValid());
    }
}
