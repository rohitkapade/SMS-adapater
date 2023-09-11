package com.tml.uep.exception;

public class EmailParsingException extends RuntimeException {
    public EmailParsingException(String errorMsg) {
        super(errorMsg);
    }

    public EmailParsingException(String errorMsg, Throwable ex) {
        super(errorMsg, ex);
    }
}
