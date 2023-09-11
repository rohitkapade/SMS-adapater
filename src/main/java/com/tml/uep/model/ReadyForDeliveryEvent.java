package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.utils.Utils;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReadyForDeliveryEvent implements OutboundEventConverter {
    @JsonProperty("AST_PRI_CELL_NUM_s")
    private String mobileNumber;

    @JsonProperty("AST_PRI_CONFST_NAME_s")
    private String firstName;

    @JsonProperty("AST_PRI_LSTNAME_s")
    private String lastName;

    @JsonProperty("REG_NUM_s")
    private String vehicleRegistrationNum;

    @JsonProperty("delivery_date")
    private OffsetDateTime deliveryDateTime;

    @JsonProperty("ASSG_FST_NAME_s")
    private String serviceAssociateFirstName;

    @JsonProperty("ASSG_LSTNAME_s")
    private String serviceAssociateLastName;

    @JsonProperty("ASSG_MOB_NUM_s")
    private String serviceAssociatePhoneNumber;

    @JsonProperty("CHASSIS_NUM_s")
    private String id;

    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("firstName", firstName);
        hashMap.put("lastName", lastName);
        if (StringUtils.isBlank(vehicleRegistrationNum)) {
            vehicleRegistrationNum = "-";
        }
        hashMap.put("vehicleRegistrationNum", vehicleRegistrationNum);
        String deliveryDateTimeFormatted = "";
        if (deliveryDateTime != null) {
            deliveryDateTimeFormatted =
                    deliveryDateTime
                            .atZoneSameInstant(ZoneId.of("Asia/Calcutta"))
                            .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
        }
        hashMap.put("deliveryDateTime", deliveryDateTimeFormatted);
        hashMap.put("serviceAssociateName", getServiceAssociateName());
        hashMap.put("serviceAssociatePhoneNumber", serviceAssociatePhoneNumber);
        return new OutboundEvent(
                Event.READY_FOR_DELIVERY,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                hashMap);
    }

    @Override
    public boolean isValid() {
        Stream<Object> fields =
                Stream.of(
                        mobileNumber,
                        vehicleRegistrationNum,
                        getServiceAssociateName(),
                        deliveryDateTime);
        return Utils.areAllNotNulls(fields);
    }

    private String getServiceAssociateName() {
        if (serviceAssociateFirstName == null && serviceAssociateLastName == null) return "";
        if (serviceAssociateFirstName == null && serviceAssociateLastName != null)
            return serviceAssociateLastName;
        if (serviceAssociateFirstName != null && serviceAssociateLastName == null)
            return serviceAssociateFirstName;
        return serviceAssociateFirstName + " " + serviceAssociateLastName;
    }
}
