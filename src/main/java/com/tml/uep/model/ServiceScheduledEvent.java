package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.utils.Utils;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
public class ServiceScheduledEvent implements OutboundEventConverter {

    @JsonProperty("SR_ID_s")
    private String serviceId;

    @JsonProperty("Customer_num")
    private String customerMobile;

    @JsonProperty("SA_first_name")
    private String serviceAdvisorFirstName;

    @JsonProperty("SA_last_name")
    private String serviceAdvisorLastName;

    @JsonProperty("REG_NUM_s")
    private String vehicleNum;

    @JsonProperty("SR_BOOKED_DATE_TIME_dt")
    private OffsetDateTime serviceDateTime;

    @JsonProperty("DIV_COMMON_NAME_s")
    private String dealershipName;

    @JsonProperty("CHASSIS_NUM_s")
    private String id;

    @Override
    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("serviceId", serviceId);
        payload.put("serviceAdvisorName", getServiceAdvisorName());
        payload.put("vehicleNum", vehicleNum);
        payload.put(
                "serviceDate", serviceDateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        payload.put("serviceTime", serviceDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        payload.put("dealershipName", dealershipName);
        return new OutboundEvent(
                Event.SERVICE_APPOINTMENT,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                customerMobile,
                payload);
    }

    private String getServiceAdvisorName() {
        return serviceAdvisorFirstName + " " + serviceAdvisorLastName;
    }

    @Override
    public boolean isValid() {
        Stream<Object> fields =
                Stream.of(
                        customerMobile,
                        vehicleNum,
                        serviceAdvisorFirstName,
                        serviceAdvisorLastName,
                        dealershipName,
                        serviceDateTime);
        return Utils.areAllNotNulls(fields);
    }
}
