package com.tml.uep;

import com.tml.uep.utils.Utils;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UtilsTest {

    @Test
    public void shouldReturnTrueIfAllAreNotNulls() {
        Stream<Object> items = Stream.of("test", "hello");
        Assert.assertTrue(Utils.areAllNotNulls(items));
    }

    @Test
    public void shouldReturnFalseIfOneOfThemIsNull() {
        Stream<Object> items = Stream.of("test", null);
        Assert.assertFalse(Utils.areAllNotNulls(items));
    }

    @Test
    public void shouldReturnFalseIfOneOfThemIsEmptyString() {
        Stream<Object> items = Stream.of("test", "hello", "");
        Assert.assertFalse(Utils.areAllNotNulls(items));
    }
}
