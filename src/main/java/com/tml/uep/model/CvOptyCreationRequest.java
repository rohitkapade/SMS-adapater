package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CvOptyCreationRequest {

    @JsonProperty("source_of_contact")
    private String sourceOfContact;

    @JsonProperty("lob_information")
    private LobInformation lobInformation;

    private Contact contact;

    @JsonProperty("tm_likely_purchase_month")
    private String tmLikelyPurchaseMonth;

    @JsonProperty("vc_data")
    private VcData vcData;

    private String quantity;

    private Division division;

    @JsonProperty("appname")
    private String appName;

    public CvOptyCreationRequest(CvOptyDetailsRequest cvOptyDetailsRequest) {
        this.sourceOfContact = "Digital-Whatsapp";
        this.lobInformation =
                new LobInformation("NA", "First Time", "NA", "ToBeUpdatedByDealership");
        this.contact =
                new Contact(
                        cvOptyDetailsRequest.getFirstName(),
                        cvOptyDetailsRequest.getLastName(),
                        cvOptyDetailsRequest.getMobileNumber(),
                        cvOptyDetailsRequest.getDistrict(),
                        cvOptyDetailsRequest.getStateCode());
        this.tmLikelyPurchaseMonth = cvOptyDetailsRequest.getLikelyPurchaseMonth();
        this.vcData =
                new VcData(
                        cvOptyDetailsRequest.getLob(),
                        cvOptyDetailsRequest.getPpl(),
                        cvOptyDetailsRequest.getPl());
        this.quantity = "1";
        this.division = new Division(cvOptyDetailsRequest.getDivisionId());
        this.appName = "engagex";
    }
}
