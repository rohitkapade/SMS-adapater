package com.tml.uep.model;

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
public class PaymentReceiptEvent implements OutboundEventConverter {
    @JsonProperty("SR_CONNUM_s")
    private String mobileNumber;

    @JsonProperty("SR_CHASISS_NUM_s")
    private String id;

    @JsonProperty("unique_id")
    private String uniqueId;

    @JsonProperty("REGISTRATION_NUM_s")
    private String registrationNumber;

    @JsonProperty("DIV_COMMON_NAME_s")
    private String dealershipName;

    @Override
    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("uniqueId", uniqueId);
        payload.put("registrationNumber", registrationNumber);
        payload.put("workshopName", dealershipName);

        return new OutboundEvent(
                Event.SERVICE_PAYMENT_RECEIPT,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                payload);
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isAnyBlank(
                this.registrationNumber, this.mobileNumber, this.dealershipName, this.uniqueId);
    }
}
