package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpportunityCreationRequest {
    private String conversationId;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String city;
    private String pincode;
    private String state;

    private String divisionId;

    @JsonProperty("pLId")
    private Long pLId;

    private String quantity;
    private LocalDate purchasedBy;
}
