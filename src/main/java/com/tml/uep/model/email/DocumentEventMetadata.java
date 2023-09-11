package com.tml.uep.model.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tml.uep.model.BusinessUnit;
import com.tml.uep.model.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentEventMetadata {

    private String transactionNumber;
    private String customerMobileNumber;
    private String vehicleRegNum;
    private String chassisNumber;
    private Event documentType;
    private BusinessUnit businessUnit;
    private String dealershipName;

    public DocumentEventMetadata createWith(
            String dealershipName, String vehicleRegNum, String chassisNumber) {
        return new DocumentEventMetadata(
                transactionNumber,
                customerMobileNumber,
                vehicleRegNum,
                chassisNumber,
                documentType,
                businessUnit,
                dealershipName);
    }
}
