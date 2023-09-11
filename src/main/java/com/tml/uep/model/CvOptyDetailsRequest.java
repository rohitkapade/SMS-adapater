package com.tml.uep.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CvOptyDetailsRequest {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String district;
    private String stateCode;
    private String lob;
    private String ppl;
    private String pl;
    private String likelyPurchaseMonth;
    private String divisionId;
    private String languageSelected;

    public CvOptyDetailsRequest(
            List<TransliterateOutString> responseList, CvOptyDetailsRequest request) {
        this.firstName = responseList.get(0).getOutString().get(0);
        this.lastName = responseList.get(1).getOutString().get(0);
        this.mobileNumber = request.mobileNumber;
        this.district = request.district;
        this.stateCode = request.stateCode;
        this.lob = request.lob;
        this.ppl = request.ppl;
        this.pl = request.pl;
        this.likelyPurchaseMonth = request.likelyPurchaseMonth;
        this.divisionId = request.divisionId;
        this.languageSelected = request.languageSelected;
    }
}
