package com.tml.uep.config.resttemplate;

import com.tml.uep.exception.ExternalSystemException;
import io.micrometer.core.instrument.util.IOUtils;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@Slf4j
public class OptyRestTemplateExceptionHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        final String error = IOUtils.toString(response.getBody());
        log.error(
                "OptyRestTemplateExceptionHandler::"
                        + "Code::"
                        + response.getStatusCode()
                        + "\nBody::"
                        + error);
        final String errorMessage = "EXTERNAL_SYSTEM_ERROR" + "\nDetails::" + error;
        if (response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError()) {
            throw new ExternalSystemException(errorMessage);
        }
    }
}
