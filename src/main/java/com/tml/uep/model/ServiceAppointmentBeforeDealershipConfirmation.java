package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.utils.Utils;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceAppointmentBeforeDealershipConfirmation implements OutboundEventConverter {
    @JsonProperty("Customer_num")
    private String mobileNumber;

    @JsonProperty("REG_NUM_s")
    private String vehicleRegistrationNum;

    @JsonProperty("CHASSIS_NUM_s")
    private String id;

    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        return new OutboundEvent(
                Event.SERVICE_APPOINTMENT_BEFORE_DEALERSHIP_CONFIRMATION,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                new HashMap<>());
    }

    @Override
    public boolean isValid() {
        Stream<Object> fields = Stream.of(mobileNumber, id);
        return Utils.areAllNotNulls(fields);
    }
}
