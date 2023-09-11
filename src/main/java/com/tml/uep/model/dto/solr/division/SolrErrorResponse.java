package com.tml.uep.model.dto.solr.division;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SolrErrorResponse {

    private String msg;

    @JsonProperty("error_code")
    private String errorCode;
}
