package com.tml.uep.config.resttemplate;

import com.tml.uep.exception.ExternalSystemException;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@Slf4j
public class RestTemplateExceptionHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return clientHttpResponse.getStatusCode().is4xxClientError()
                || clientHttpResponse.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        final String error = IOUtils.toString(clientHttpResponse.getBody());
        log.error(
                "RestTemplateExceptionHandler::"
                        + "Code::"
                        + clientHttpResponse.getStatusCode()
                        + "\nBody::"
                        + error);
        final String errorMessage = "EXTERNAL_SYSTEM_ERROR" + "\nDetails::" + error;
        if (clientHttpResponse.getStatusCode().is4xxClientError()) {
            throw new ExternalSystemException(errorMessage, clientHttpResponse.getStatusCode());
        }
        if (clientHttpResponse.getStatusCode().is5xxServerError()) {
            throw new ExternalSystemException(errorMessage, clientHttpResponse.getStatusCode());
        }
    }
}
