package com.tml.uep.service;

import com.tml.uep.config.AWSTestConfiguration;
import com.tml.uep.exception.EmailParsingException;
import com.tml.uep.model.EmailMessage;
import com.tml.uep.testUtils.TestMailUtils;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.mail.internet.MimeMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@Import({AWSTestConfiguration.class})
@RunWith(SpringRunner.class)
public class EmailParserServiceTest {

    @Autowired private EmailParserService service;

    private ClassLoader classLoader = getClass().getClassLoader();

    @Value("${domain.crm-sender-email}")
    private String senderEmail;

    @Test
    public void shouldParseEmailFromString() throws Exception {
        String subject = "SERVICE_INVOICE";
        TestMailUtils mailUtils = new TestMailUtils();
        URL url = classLoader.getResource("emails/serviceInvoice.json");
        String emailBody = Files.readString(Path.of(url.toURI()));
        MimeMessage messageWithAttachment =
                mailUtils.createMessageWithAttachment(
                        subject, emailBody, "pdfs/CVBUServiceInvoice.pdf", senderEmail);
        String mail = mailUtils.getMailAsString(messageWithAttachment);
        EmailMessage emailMessage = service.parse(mail.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull(emailMessage);
        Assert.assertNotNull(emailMessage.getAttachments());
        Assert.assertEquals(subject, emailMessage.getSubject());
        Assert.assertEquals(emailBody, emailMessage.getBody());
        Assert.assertEquals(1, emailMessage.getAttachments().size());
    }

    @Test(expected = EmailParsingException.class)
    public void shouldThrowExceptionForNonCRMEmails() throws Exception {
        String subject = "SERVICE_INVOICE";
        TestMailUtils mailUtils = new TestMailUtils();
        URL url = classLoader.getResource("emails/serviceInvoice.json");
        String emailBody = Files.readString(Path.of(url.toURI()));
        MimeMessage messageWithAttachment =
                mailUtils.createMessageWithAttachment(
                        subject, emailBody, "pdfs/CVBUServiceInvoice.pdf", "test@test.com");
        String mail = mailUtils.getMailAsString(messageWithAttachment);
        EmailMessage emailMessage = service.parse(mail.getBytes(StandardCharsets.UTF_8));
    }
}
