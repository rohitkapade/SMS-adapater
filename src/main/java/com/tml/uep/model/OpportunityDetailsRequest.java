package com.tml.uep.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpportunityDetailsRequest {
    @JsonProperty("opty_id")
    @JsonAlias({"opty_id", "optyIds"})
    @NotEmpty
    private List<String> optyIds;
}
