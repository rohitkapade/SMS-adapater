package com.tml.uep;

import static org.junit.Assert.assertEquals;

import com.tml.uep.utils.DateUtils;
import java.time.LocalDate;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DateUtilsTest {

    @Test
    public void shouldReturnFormattedDateForGivenLocalDate() {
        String result = DateUtils.getFormattedDateWithoutTime(LocalDate.of(2021, 10, 29));
        assertEquals("10/29/2021", result);
    }
}
