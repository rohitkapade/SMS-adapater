package com.tml.uep.solr_api.dto.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Address {
    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    private String city;
    private String district;
    private String pincode;
    private State state;
    private String taluka;

    public Address(
            CustomerSolrResponse customerSolrResponse,
            OpportunityCreationRequest opportunityCreationRequest) {
        this.addressLine1 = customerSolrResponse.getAddress().getAddressLine1();
        this.addressLine2 = customerSolrResponse.getAddress().getAddressLine2();
        this.city =
                !StringUtils.isEmpty(opportunityCreationRequest.getCity())
                        ? opportunityCreationRequest.getCity()
                        : customerSolrResponse.getAddress().getCity();
        this.district = customerSolrResponse.getAddress().getCity();
        this.pincode =
                !StringUtils.isEmpty(opportunityCreationRequest.getPincode())
                        ? opportunityCreationRequest.getPincode()
                        : customerSolrResponse.getAddress().getPincode();
        this.state =
                !StringUtils.isEmpty(opportunityCreationRequest.getState())
                        ? new State("", opportunityCreationRequest.getState())
                        : new State("", customerSolrResponse.getAddress().getState().getName());
        this.taluka = customerSolrResponse.getAddress().getTaluka();
    }

    public Address(OpportunityCreationRequest opportunityCreationRequest) {
        this.addressLine1 =
                !StringUtils.isEmpty(opportunityCreationRequest.getCity())
                        ? opportunityCreationRequest
                                .getCity()
                                .concat(
                                        opportunityCreationRequest
                                                .getMobileNumber()) // As Address Line 1 has to be
                        // unique for a customer in CRM.
                        : "";
        this.addressLine2 = "";
        this.city = getAddressFieldOrEmptyString(opportunityCreationRequest.getCity());
        this.district = getAddressFieldOrEmptyString(opportunityCreationRequest.getCity());
        this.pincode = getAddressFieldOrEmptyString(opportunityCreationRequest.getPincode());
        this.state =
                !StringUtils.isEmpty(opportunityCreationRequest.getState())
                        ? new State("", opportunityCreationRequest.getState())
                        : new State("", "");
        this.taluka = getAddressFieldOrEmptyString(opportunityCreationRequest.getCity());
    }

    private String getAddressFieldOrEmptyString(String addressField) {
        return !StringUtils.isEmpty(addressField) ? addressField : "";
    }
}
