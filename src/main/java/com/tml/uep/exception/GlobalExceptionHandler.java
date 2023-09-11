package com.tml.uep.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tml.uep.model.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired private ObjectMapper mapper;

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<Object> handleClientError(HttpClientErrorException e) throws JsonProcessingException {
        logger.error(e.getStatusText() + ", status:" + e.getStatusCode(), e);

        ErrorResponse errorResponse = getErrorResponse(e.getStatusText());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleRequestClientErrorMissingParam(
            MissingServletRequestParameterException ex) throws JsonProcessingException {
        logger.error(ex.getMessage() + ", status: 400", ex);

        ErrorResponse errorResponse = getErrorResponse(ex.getMessage());
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleRequestClientError(MethodArgumentNotValidException ex)
            throws JsonProcessingException {
        logger.error(ex.getMessage() + ", status: 400", ex);
        return handleArgumentValidationError(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleRequestClientErrorForArgumentMismatch(
            MethodArgumentTypeMismatchException ex) throws JsonProcessingException {
        logger.error(ex.getMessage() + ", status: 400", ex);
        String message = "Invalid value for parameter " + ex.getName();
        ErrorResponse errorResponse = getErrorResponse(message);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<Object> handleRequestClientError(BindException ex)
            throws JsonProcessingException {
        logger.error(ex.getMessage() + ", status: 400", ex);
        return handleArgumentValidationError(ex.getBindingResult().getFieldErrors());
    }

    private ResponseEntity<Object> handleArgumentValidationError(List<FieldError> fieldErrors)
            throws JsonProcessingException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", "400");

        List<String> errors =
                fieldErrors.stream()
                        .map(error -> error.getField() + " " + error.getDefaultMessage())
                        .collect(Collectors.toList());

        body.put("errors", errors);
        String errorBody = mapper.writeValueAsString(body);
        ErrorResponse errorResponse = mapper.readValue(errorBody, ErrorResponse.class);
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex)
            throws JsonProcessingException {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = ife.getPath().get(0).getFieldName();
            String message = "Invalid value for " + fieldName;
            ErrorResponse errorResponse = getErrorResponse(message);
            return ResponseEntity.badRequest().body(errorResponse);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleAllException(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private ErrorResponse getErrorResponse(String message) throws JsonProcessingException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", "400");
        body.put("errors", List.of(message));

        String errorBody = mapper.writeValueAsString(body);
        return mapper.readValue(errorBody, ErrorResponse.class);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> sizeException(ConstraintViolationException e) throws Exception{
        ErrorResponse errorResponse = getErrorResponse(e.getMessage());
        return ResponseEntity.status(400).body(errorResponse);
    }
}
