package com.tml.uep.utils;

import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Utils {

    @Autowired
    private ObjectMapper mapper;

    public static boolean areAllNotNulls(Stream<Object> items) {
        return items.allMatch(
                (item) -> {
                    boolean isNull = Objects.isNull(item);
                    if (isNull) return false;
                    return !Objects.equals("", item.toString());
                });
    }

    public static String convertToBase64Image(Path path, String format) {
        byte[] imageAsBytes = new byte[0];
        try {
            Resource resource = new UrlResource(path.toUri());
            InputStream inputStream = resource.getInputStream();
            imageAsBytes = IOUtils.toByteArray(inputStream);

        } catch (IOException e) {
            log.error("Exception occured while converting to base64 {}", e.getMessage());
        }
        return "data:image/"
                + format
                + ";base64,"
                + Base64.getEncoder().encodeToString(imageAsBytes);
    }

    public <T> T toType(String input, Class<T> valueType) {
        Supplier<RuntimeException> ex =
                () -> new RuntimeException("Failed to convert String to object");
        return Try.of(() -> mapper.readValue(input, valueType)).getOrElseThrow(ex);
    }
}
