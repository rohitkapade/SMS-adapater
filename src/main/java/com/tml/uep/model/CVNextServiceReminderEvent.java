package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.utils.Utils;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CVNextServiceReminderEvent implements OutboundEventConverter {

    @JsonProperty("CON_FSTNAME_s")
    private String firstName;

    @JsonProperty("CON_CELL_PH_NUM_s")
    private String customerMobile;

    @JsonProperty("PPL_s")
    private String ppl;

    @JsonProperty("CHASSIS_NUM_s")
    private String chassisNumber;

    @JsonProperty("CON_LSTNAME_s")
    private String lastName;

    @JsonProperty("NEXT_SRVC_DUE_dt")
    private String nextServiceDueDate;

    @JsonProperty("CON_EMAIL_s")
    private String email;

    @JsonProperty("REGISTRATION_NUM_s")
    private String registrationNumber;

    @Override
    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("registrationNumber", registrationNumber);
        return new OutboundEvent(
                Event.NEXT_SERVICE_REMINDER,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                customerMobile,
                payload);
    }

    @Override
    public String getId() {
        return chassisNumber;
    }

    @Override
    public boolean isValid() {
        Stream<Object> fields = Stream.of(customerMobile, registrationNumber);
        return Utils.areAllNotNulls(fields);
    }
}
