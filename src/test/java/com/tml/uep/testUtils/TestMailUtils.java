package com.tml.uep.testUtils;

import com.tml.uep.utils.MailUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.mail.util.MimeMessageParser;

public class TestMailUtils {

    private ClassLoader classLoader = getClass().getClassLoader();

    public String getMailAsString(MimeMessage mail) throws Exception {

        MimeMessageParser attachmentMessageParser = new MimeMessageParser(mail);
        attachmentMessageParser.parse();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mail.writeTo(outputStream);
        return outputStream.toString();
    }

    public MimeMessage createMessageWithAttachment(
            String subject, String emailBody, String attachmentFilePath, String senderEmail)
            throws MessagingException, IOException, URISyntaxException {
        MimeMessage message = new MimeMessage(MailUtils.createMockSession());
        message.setSubject(subject);
        message.setFrom(senderEmail);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(emailBody);
        MimeBodyPart attachmentPart = new MimeBodyPart();
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(attachmentFilePath);
        File file = new File(url.toURI());
        attachmentPart.attachFile(file);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentPart);
        message.setContent(multipart);
        return message;
    }

    public String createStringifiedEmail(
            String subject, String bodyFilePath, String attachmentFilePath, String from)
            throws Exception {

        TestMailUtils mailUtils = new TestMailUtils();
        URL url = classLoader.getResource(bodyFilePath);
        String emailBody = Files.readString(Path.of(url.toURI()));
        MimeMessage messageWithAttachment =
                mailUtils.createMessageWithAttachment(subject, emailBody, attachmentFilePath, from);
        return mailUtils.getMailAsString(messageWithAttachment);
    }
}
