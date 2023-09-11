package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.kafka.OutboundEvent;
import com.tml.uep.utils.Utils;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WelcomeLetterEvent implements OutboundEventConverter {
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

    @JsonProperty("VEH_MODEL_s")
    private String vehicleName;

    @JsonProperty("customer_addr1")
    private String customerAddress1;

    @JsonProperty("customer_addr2")
    private String customerAddress2;

    @JsonProperty("customer_city")
    private String city;

    @JsonProperty("customer_state")
    private String state;

    @JsonProperty("customer_country")
    private String country;

    @JsonProperty("SELLING_DLR_s")
    private String dealerName;

    @JsonProperty("DLR_CITY_s")
    private String dealerCity;

    public WelcomeLetterEvent(
            String mobileNumber,
            String firstName,
            String lastName,
            String vehicleNumber,
            OffsetDateTime dateTime,
            String chassisNum) {
        this.mobileNumber = mobileNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vehicleRegistrationNum = vehicleNumber;
        this.actualDeliveryDate = dateTime;
        this.id = chassisNum;
    }

    public OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("firstName", firstName);
        hashMap.put("lastName", lastName);
        hashMap.put("vehicleRegistrationNum", vehicleRegistrationNum);
        hashMap.put("actualDeliveryDate", actualDeliveryDate);

        return new OutboundEvent(
                Event.WELCOME_LETTER,
                UUID.randomUUID().toString(),
                OffsetDateTime.now(),
                businessUnit,
                mobileNumber,
                hashMap);
    }

    public HashMap<String, String> getPlaceholderMap() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URI androidQrUrl =
                Objects.requireNonNull(classLoader.getResource("static/images/android_qr.png"))
                        .toURI();
        URI appleQrUrl =
                Objects.requireNonNull(classLoader.getResource("static/images/apple_qr.png"))
                        .toURI();
        HashMap<String, String> placeholderMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = formatter.format(OffsetDateTime.now());
        Path androidQR = Paths.get(androidQrUrl);
        Path appleQR = Paths.get(appleQrUrl);
        String androidQRImg = Utils.convertToBase64Image(androidQR, "png");
        String appleQRImg = Utils.convertToBase64Image(appleQR, "png");
        placeholderMap.put("vehicleName", vehicleName);
        placeholderMap.put("customerName", getCustomerName());
        placeholderMap.put("address1", customerAddress1);
        placeholderMap.put(
                "address2", StringUtils.isEmpty(customerAddress2) ? " " : customerAddress2);
        placeholderMap.put("city", city);
        placeholderMap.put("state", state);
        placeholderMap.put("country", country);
        placeholderMap.put("date", dateString);
        placeholderMap.put("dealerName", dealerName);
        placeholderMap.put("dealerCity", dealerCity);
        placeholderMap.put("androidQR", androidQRImg);
        placeholderMap.put("appleQR", appleQRImg);
        return placeholderMap;
    }

    private String getCustomerName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isAnyBlank(
                mobileNumber,
                vehicleName,
                getCustomerName().trim(),
                customerAddress1,
                city,
                state,
                country,
                dealerName,
                dealerCity);
    }
}
