package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SolrOptyDetailsResponse {

    @JsonAlias("opty_details")
    private List<OpportunityDetails> optyDetails;
}
