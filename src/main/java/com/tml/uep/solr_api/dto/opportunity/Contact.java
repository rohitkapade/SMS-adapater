package com.tml.uep.solr_api.dto.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Contact {
    @JsonProperty("contact_id")
    private String contactId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @ToString.Exclude
    @JsonProperty("mobile_number")
    private String mobileNumber;

    public Contact(
            CustomerSolrResponse customerSolrResponse,
            OpportunityCreationRequest opportunityCreationRequest) {
        this.contactId = customerSolrResponse.getContactId();
        this.firstName =
                !StringUtils.isEmpty(opportunityCreationRequest.getFirstName())
                        ? opportunityCreationRequest.getFirstName()
                        : customerSolrResponse.getFirstName();
        this.lastName =
                !StringUtils.isEmpty(opportunityCreationRequest.getLastName())
                        ? opportunityCreationRequest.getLastName()
                        : customerSolrResponse.getLastName();
        this.mobileNumber =
                getMobileNumberWithoutCountryCode(opportunityCreationRequest.getMobileNumber());
    }

    public Contact(OpportunityCreationRequest opportunityCreationRequest) {
        this.contactId = "";
        this.firstName =
                !StringUtils.isEmpty(opportunityCreationRequest.getFirstName())
                        ? opportunityCreationRequest.getFirstName()
                        : "";
        this.lastName =
                !StringUtils.isEmpty(opportunityCreationRequest.getLastName())
                        ? opportunityCreationRequest.getLastName()
                        : opportunityCreationRequest.getFirstName();
        this.mobileNumber =
                getMobileNumberWithoutCountryCode(opportunityCreationRequest.getMobileNumber());
    }

    private String getMobileNumberWithoutCountryCode(String phone) {
        return phone.length() > 10 ? phone.substring(phone.length() - 10) : phone;
    }
}
