package com.tml.uep.model.dto.solr.division;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DivisionListRequest {

    @NotBlank private String state;

    @NotBlank private String city;
}
