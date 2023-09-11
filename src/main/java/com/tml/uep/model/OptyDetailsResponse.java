package com.tml.uep.model;

import com.tml.uep.model.entity.CvOpportunity;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptyDetailsResponse {
    private String phoneNumber;
    private String opportunityId;
    private OffsetDateTime dateTime;

    public OptyDetailsResponse(CvOpportunity cvOpportunity) {
        this.dateTime = cvOpportunity.getDateTime();
        this.opportunityId = cvOpportunity.getOpportunityId();
        this.phoneNumber = cvOpportunity.getPhoneNumber();
    }
}
