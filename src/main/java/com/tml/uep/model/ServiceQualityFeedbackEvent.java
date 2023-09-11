package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceQualityFeedbackEvent implements OutboundEventConverter {

    @JsonProperty("SR_CONNUM_s")
    private String mobileNumber;

    @JsonProperty("SR_CHASISS_NUM_s")
    private String id;

    @JsonProperty("SR_VEHICLE_REG_NUM_s")
    private String registrationNumber;

    @JsonProperty("SR_ORG_NAME_s")
    private String dealershipName;

    @Override
    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("registrationNumber", registrationNumber);
        payload.put("workshopName", dealershipName);
        return new OutboundEvent(
                Event.SERVICE_QUALITY_FEEDBACK_LINK,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                payload);
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isAnyBlank(
                this.registrationNumber, this.mobileNumber, this.dealershipName);
    }
}
