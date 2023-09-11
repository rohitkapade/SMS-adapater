package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpportunityDetails {

    @JsonAlias("opty_stage")
    private String optyStage;

    @JsonAlias("lead_ph_no")
    private String leadPhoneNumber;

    @JsonAlias("vehicle desc")
    private String vehicleDescription;

    @JsonAlias("sales_city")
    private String salesCity;

    @JsonAlias("org_state")
    private String orgState;

    @JsonAlias("pr_contact_last_name")
    private String primaryContactLastName;

    @JsonAlias("pr_contact_fst_name")
    private String primaryContactFirstName;

    private String pl;

    @JsonAlias("pr_contact_dist")
    private String primaryContactDistrict;

    @JsonAlias("sales_state")
    private String salesState;

    @JsonAlias("lead_fst_name")
    private String leadFirstName;

    private String channel;

    @JsonAlias("dealer_code")
    private String dealerCode;

    @JsonAlias("customer_type")
    private String customerType;

    @JsonAlias("opty_id")
    private String optyId;

    private String lob;

    @JsonAlias("lead_last_name")
    private String leadLastName;

    @JsonAlias("org_name")
    private String orgName;
}
