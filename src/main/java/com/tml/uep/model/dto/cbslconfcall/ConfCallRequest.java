package com.tml.uep.model.dto.cbslconfcall;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ConfCallRequest {

    @NotBlank
    @JsonProperty("sys_phone")
    @JsonAlias({"mobileNumber", "sys_phone"})
    private String mobileNumber;
}
