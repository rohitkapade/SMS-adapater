package com.tml.uep.utils;

import static com.tml.uep.data.CustomerQueryData.IMAGES_URL;
import static org.junit.jupiter.api.Assertions.*;

import com.tml.uep.config.AWSTestConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import org.apache.tika.mime.MimeTypeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(AWSTestConfiguration.class)
public class FileUtilsTest {

    @Test
    public void shouldReturnFileExtensionWhenGivenValidMimeType() throws MimeTypeException {

        String fileExtension = FileUtils.getFileExtension("image/png");

        assertEquals(".png", fileExtension);
    }

    @Test(expected = MimeTypeException.class)
    public void shouldThrowMimeTypeExceptionWhenGivenInvalidMimeType() throws MimeTypeException {

        String fileExtension = FileUtils.getFileExtension("test");
    }

    @Test
    public void shouldReturnMimeTypeWhenGivenValidFile() {

        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(IMAGES_URL);
        File file = null;
        byte[] bytes;
        try {
            file = new File(url.toURI());
            bytes = Files.readAllBytes(file.toPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String mimeType = FileUtils.getMimeType(bytes);

        assertEquals("image/png", mimeType);
    }

    @Test
    public void shouldReturnOctetStreamMimeTypeWhenGivenUnkownFile() {

        String mimeType = FileUtils.getMimeType(new byte[5]);

        assertEquals("application/octet-stream", mimeType);
    }
}
