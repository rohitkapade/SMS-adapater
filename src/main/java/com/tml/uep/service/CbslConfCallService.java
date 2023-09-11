package com.tml.uep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tml.uep.exception.ExternalSystemException;
import com.tml.uep.model.CbslConfCallResponse;
import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.dto.cbslconfcall.CbslConfCallDetails;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRecordingRequest;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRequest;
import com.tml.uep.model.entity.CbslConfCallEntity;
import com.tml.uep.repository.CbslConfCallResponseRepository;
import com.tml.uep.utils.MaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tml.uep.utils.DateUtils.convertToOffsetDateTimeFromDateString;

@Service
@Slf4j
public class CbslConfCallService {

    @Autowired private RestTemplate restTemplate;

    @Autowired private ObjectMapper mapper;

    @Autowired private CbslConfCallResponseRepository repository;

    @Value("${cbsl-conf-api.base-url}")
    private String BASE_URL;

    @Value("${cbsl-conf-api.conf-call-endpoint}")
    private String CONF_CALL_ENDPOINT;

    @Value("${cbsl-conf-api.auth-key}")
    private String authKey;

    public String initiateCall(ConfCallRequest confCallRequest) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.put("Authkey", List.of(authKey));
            HttpEntity<Map<String, Object>> entity = new HttpEntity(confCallRequest, headers);

            log.info("Cbsl Conf call request: {} ", confCallRequest);
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(
                            getApiUrl(confCallRequest.getMobileNumber()),
                            entity,
                            String.class,
                            headers);

            log.info("Cbsl conf call api response : {}", responseEntity.getBody());
            CbslConfCallResponse cbslConfCallResponse =
                    mapper.readValue(responseEntity.getBody(), CbslConfCallResponse.class);
            CbslConfCallEntity cbslConfCallEntity =
                    new CbslConfCallEntity(cbslConfCallResponse.getCall());
            repository.save(cbslConfCallEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info(
                        "Cbsl conf call api returned success for : {} ",
                        MaskingUtils.maskMobileNumber(confCallRequest.getMobileNumber()));
                return responseEntity.getBody();
            } else {
                log.error(
                        "Cbsl conf call api returned status code: {} and body: {}",
                        responseEntity.getStatusCode(),
                        responseEntity.getBody());
                throw new ExternalSystemException(responseEntity.getBody());
            }

        } catch (Exception e) {
            log.error(
                    "Unable to call cbsl conf call api for customer {} ",
                    MaskingUtils.maskMobileNumber(confCallRequest.getMobileNumber()),
                    e);
            throw new ExternalSystemException(e.getMessage());
        }
    }

    private String getApiUrl(String mobileNumber) {
        return String.format("%s%s?phone=%s", BASE_URL, CONF_CALL_ENDPOINT, mobileNumber);
    }

    public boolean updateRecordingDetails(ConfCallRecordingRequest request) {
        CbslConfCallEntity entity = getConfCallEntity(request);
        repository.save(entity);
        return true;
    }

    private CbslConfCallEntity getConfCallEntity(ConfCallRecordingRequest request) {
        Optional<CbslConfCallEntity> confCallEntityOptional = repository.findById(request.getSid());
        if (confCallEntityOptional.isEmpty()) {
            log.error(
                    "Conf call entity for sid {} does not exist. Request {}",
                    request.getSid(),
                    request);
            return new CbslConfCallEntity(
                    request, CbslConfCallStatus.RECORDING_AVAILABLE_WITHOUT_REFERENCE);
        }

        CbslConfCallEntity entity = confCallEntityOptional.get();
        entity.updateConfCallEntityFromRecordingRequest(request);
        return entity;
    }

    public List<CbslConfCallDetails> getCbslConfCallRecords(
            CbslConfCallStatus status, String startDate, String endDate) {

        return repository
                .findAllByStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
                        convertToOffsetDateTimeFromDateString(startDate),
                        convertToOffsetDateTimeFromDateString(endDate),
                        status)
                .stream()
                .map(CbslConfCallDetails::new)
                .collect(Collectors.toList());
    }
}
