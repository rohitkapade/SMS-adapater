package com.tml.uep.model.dto.cbslconfcall;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ConfCallRecordingRequest {

    @JsonProperty("CallFrom")
    private String callFrom;

    @JsonProperty("CallSid")
    @NotBlank
    private String sid;

    @JsonProperty("DialCallDuration")
    private String dialCallDuration;

    @JsonProperty("StartTime")
    private String startTime;

    @JsonProperty("EndTime")
    private String endTime;

    @JsonProperty("RecordingUrl")
    @NotBlank
    private String recordingUrl;
}
