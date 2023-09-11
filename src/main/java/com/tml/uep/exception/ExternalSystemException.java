package com.tml.uep.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ExternalSystemException extends RuntimeException {

    private HttpStatus httpStatus;
    public ExternalSystemException(String message) {
        super(message);
    }

    public ExternalSystemException(String message, Throwable ex) {
        super(message, ex);
    }

    public ExternalSystemException(String message,HttpStatus httpStatus ) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
