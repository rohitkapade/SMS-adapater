package com.tml.uep.model.dto.dealership.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Dealership {

    @JsonProperty(value = "DIV_NAME_s")
    private String divName;
    @JsonProperty(value = "DIV_ID_s")
    private String divId;
    @JsonProperty(value = "ORG_NAME_s")
    private String orgName;
    @JsonProperty(value = "ORG_ADD")
    private String orgAddress;
}
