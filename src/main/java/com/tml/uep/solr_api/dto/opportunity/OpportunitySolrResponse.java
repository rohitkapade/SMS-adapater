package com.tml.uep.solr_api.dto.opportunity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpportunitySolrResponse {
    private String id;
    private String msg;

    @JsonProperty("error_code")
    private String errorCode;
}
