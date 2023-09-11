package com.tml.uep.service;

import com.tml.uep.exception.EmailParsingException;
import com.tml.uep.model.EmailMessage;
import com.tml.uep.utils.MailUtils;
import java.util.List;
import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailParserService {

    @Value("${domain.crm-sender-email}")
    private String crmSenderEmail;

    public EmailMessage parse(byte[] emailContent) throws Exception {

        MimeMessage message =
                MimeMessageUtils.createMimeMessage(MailUtils.createMockSession(), emailContent);
        MimeMessageParser messageParser = new MimeMessageParser(message);
        messageParser.parse();

        if (!crmSenderEmail.equalsIgnoreCase(messageParser.getFrom())) {
            throw new EmailParsingException("Email sender other than CRM");
        }

        List<DataSource> attachments = messageParser.getAttachmentList();
        if (attachments.isEmpty()) {
            throw new EmailParsingException("Email doesn't contain any attachment");
        }
        String body = messageParser.getPlainContent();
        if (StringUtils.isAllBlank(body)) {
            throw new EmailParsingException("Email doesn't contain body");
        }
        String subject = messageParser.getSubject();
        if (StringUtils.isAllBlank(subject)) {
            throw new EmailParsingException("Email doesn't contain subject");
        }
        return EmailMessage.builder().subject(subject).body(body).attachments(attachments).build();
    }
}
