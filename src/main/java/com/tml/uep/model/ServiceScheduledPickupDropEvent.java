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
public class ServiceScheduledPickupDropEvent implements OutboundEventConverter {

    @JsonProperty("Customer_num")
    private String customerMobile;

    @JsonProperty("CHASSIS_NUM_s")
    private String id;

    @Override
    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        return new OutboundEvent(
                Event.SERVICE_APPOINTMENT_PICKUP_DROP,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                customerMobile,
                new HashMap<>());
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isEmpty(customerMobile);
    }
}
