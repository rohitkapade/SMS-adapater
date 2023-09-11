package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LobInformation {

    @JsonProperty("vehicle_application")
    private String vehicleApplication;

    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("body_type")
    private String bodyType;

    @JsonProperty("usage_category")
    private String usageCategory;
}
