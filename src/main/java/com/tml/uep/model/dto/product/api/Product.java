package com.tml.uep.model.dto.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Product {

    @JsonProperty(value = "PL_ID_s")
    private String id;
    @JsonProperty(value = "PL_NAME_s")
    private String name;
    @JsonProperty(value = "PR_CONTACT_CELL_PH_NUM_s")
    private String phoneNumber;

}
