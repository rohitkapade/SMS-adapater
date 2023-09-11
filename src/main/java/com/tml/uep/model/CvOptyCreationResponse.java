package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CvOptyCreationResponse {

    @JsonProperty("opty_id")
    private String opportunityId;

    @JsonProperty("msg")
    private String message;
}
