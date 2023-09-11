package com.tml.uep.model.dto.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ProductRequest {

    @JsonProperty(value = "mobile_number")
    private String mobileNumber;
}
