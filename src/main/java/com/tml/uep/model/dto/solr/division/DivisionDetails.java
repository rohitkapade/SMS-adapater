package com.tml.uep.model.dto.solr.division;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DivisionDetails {

    @JsonAlias("org-name")
    private String orgName;

    @JsonAlias("div_id")
    private String divisionId;

    @JsonAlias("div_name")
    private String divisionName;

    @JsonAlias("dealer_code")
    private String dealerCode;

    private String taluka;

    private String city;

    private String district;

    private String state;

    @JsonAlias("address1")
    private String addressLine1;

    @JsonAlias("address2")
    private String addressLine2;
}
