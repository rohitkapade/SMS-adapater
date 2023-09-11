package com.tml.uep.exception;

public class FileParserException extends RuntimeException {

    public FileParserException(String errorMsg) {
        super(errorMsg);
    }

    public FileParserException(String errorMsg, Throwable ex) {
        super(errorMsg, ex);
    }
}
