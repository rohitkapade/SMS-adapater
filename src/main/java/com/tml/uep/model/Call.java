package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Call {

    @JsonProperty("Sid")
    private String sid;

    @JsonProperty("From")
    private String from;

    @JsonProperty("StartTime")
    private String startTime;

    @JsonProperty("Status")
    private String status;
}
