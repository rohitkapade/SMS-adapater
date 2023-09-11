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
public class ServicingBenefitsEvent implements OutboundEventConverter {
    @JsonProperty("CON_CELL_PH_NUM_s")
    private String mobileNumber;

    @JsonProperty("CON_FSTNAME_s")
    private String firstName;

    @JsonProperty("CON_LSTNAME_s")
    private String lastName;

    @JsonProperty("REGISTRATION_NUM_s")
    private String vehicleRegistrationNum;

    @JsonProperty("ACTL_DELVIRY_DATE_dt")
    private OffsetDateTime actualDeliveryDate;

    @JsonProperty("CHASSIS_NUM_s")
    private String id;

    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("vehicleRegistrationNum", vehicleRegistrationNum);
        hashMap.put("actualDeliveryDate", actualDeliveryDate);
        return new OutboundEvent(
                Event.SERVICING_BENEFITS,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                hashMap);
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isBlank(this.mobileNumber);
    }
}
