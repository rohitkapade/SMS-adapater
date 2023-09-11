package com.tml.uep.utils;

import java.util.Properties;
import javax.mail.Session;

public class MailUtils {

    private MailUtils() {
        throw new RuntimeException(
                "This utility class has static methods and doesnt need instantiation.");
    }

    public static Session createMockSession() {
        java.util.Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "myhost");
        return Session.getDefaultInstance(props);
    }
}
