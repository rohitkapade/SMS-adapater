package com.tml.uep.solr_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.Address;
import lombok.Getter;

@Getter
public class CustomerSolrResponse {

    @JsonProperty("contact_id")
    private String contactId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private Address address;
}
