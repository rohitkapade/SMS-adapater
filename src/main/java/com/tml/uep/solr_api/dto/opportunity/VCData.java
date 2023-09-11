package com.tml.uep.solr_api.dto.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tml.uep.model.entity.ProductLineVcMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class VCData {
    private String lob;

    @JsonProperty("vc_number")
    private String vcNumber;

    private String ppl;

    private String pl;

    public VCData(ProductLineVcMapping productLineVcMapping) {
        this.lob = productLineVcMapping.getLob();
        this.vcNumber = productLineVcMapping.getVcNumber();
        this.ppl = productLineVcMapping.getPpl();
        this.pl = productLineVcMapping.getPl();
    }
}
