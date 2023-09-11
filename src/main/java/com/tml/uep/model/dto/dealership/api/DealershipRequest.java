package com.tml.uep.model.dto.dealership.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DealershipRequest {

    @JsonProperty(value = "mobile_number")
    private String mobileNumber;

    public DealershipRequest(String mobile_number){
        this.mobileNumber = mobile_number;
    }
}
