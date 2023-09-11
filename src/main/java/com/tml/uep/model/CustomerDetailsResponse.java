package com.tml.uep.model;

import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerDetailsResponse {
    private String name;
    private String city;
    private String state;
    private String pincode;
    private String email;

    public CustomerDetailsResponse(CustomerSolrResponse customerSolrResponse) {
        this.name = customerSolrResponse.getFirstName() + " " + customerSolrResponse.getLastName();
        this.email = customerSolrResponse.getEmail();
        this.city = customerSolrResponse.getAddress().getCity();
        this.state = customerSolrResponse.getAddress().getState().getName();
        this.pincode = customerSolrResponse.getAddress().getPincode();
    }
}
