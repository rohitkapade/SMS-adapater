package com.tml.uep.solr_api.dto.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.OpportunityCreationRequest;
import com.tml.uep.model.entity.ProductLineVcMapping;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import com.tml.uep.utils.DateUtils;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class OpportunitySolrRequest {

    @JsonProperty("division_id")
    private String divisionId;

    private Contact contact;

    @JsonProperty("vc_data")
    private VCData vcData;

    @ToString.Exclude private Address address;

    private String quantity;

    @JsonProperty("purchased_by")
    private String purchasedBy;

    private String quality;

    public OpportunitySolrRequest(
            String contactId,
            ProductLineVcMapping productLineVcMapping,
            OpportunityCreationRequest opportunityCreationRequest) {
        this.divisionId = opportunityCreationRequest.getDivisionId();
        this.contact = new Contact(contactId, null, null, null);
        this.vcData = new VCData(productLineVcMapping);
        this.address = new Address();
        this.quantity =
                !StringUtils.isEmpty(opportunityCreationRequest.getQuantity())
                        ? opportunityCreationRequest.getQuantity()
                        : "1";
        this.purchasedBy =
                DateUtils.getFormattedDateWithoutTime(opportunityCreationRequest.getPurchasedBy());
        this.quality = determineQuality(opportunityCreationRequest.getPurchasedBy());
    }

    public OpportunitySolrRequest(
            CustomerSolrResponse customerSolrResponse,
            ProductLineVcMapping productLineVcMapping,
            OpportunityCreationRequest opportunityCreationRequest) {
        this.divisionId = opportunityCreationRequest.getDivisionId();
        this.contact = new Contact(customerSolrResponse, opportunityCreationRequest);
        this.vcData = new VCData(productLineVcMapping);
        this.address = new Address(customerSolrResponse, opportunityCreationRequest);
        this.quantity =
                !StringUtils.isEmpty(opportunityCreationRequest.getQuantity())
                        ? opportunityCreationRequest.getQuantity()
                        : "1";
        this.purchasedBy =
                DateUtils.getFormattedDateWithoutTime(opportunityCreationRequest.getPurchasedBy());
        this.quality = determineQuality(opportunityCreationRequest.getPurchasedBy());
    }

    public OpportunitySolrRequest(
            ProductLineVcMapping productLineVcMapping,
            OpportunityCreationRequest opportunityCreationRequest) {
        this.divisionId = opportunityCreationRequest.getDivisionId();
        this.contact = new Contact(opportunityCreationRequest);
        this.vcData = new VCData(productLineVcMapping);
        this.address = new Address(opportunityCreationRequest);
        this.quantity =
                !StringUtils.isEmpty(opportunityCreationRequest.getQuantity())
                        ? opportunityCreationRequest.getQuantity()
                        : "1";
        this.purchasedBy =
                DateUtils.getFormattedDateWithoutTime(opportunityCreationRequest.getPurchasedBy());
        this.quality = determineQuality(opportunityCreationRequest.getPurchasedBy());
    }

    private String determineQuality(LocalDate purchasedBy) {
        if (purchasedBy.isBefore(purchasedBy.plusDays(30))) {
            return String.valueOf(Quality.Hot);
        } else if (purchasedBy.isAfter(purchasedBy.plusDays(30))
                && purchasedBy.isBefore(purchasedBy.plusDays(60))) {
            return String.valueOf(Quality.Warm);
        } else return String.valueOf(Quality.Cold);
    }
}
