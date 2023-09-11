package com.tml.uep.model.dto.solr.city;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CityListRequest {

    @NotBlank private String state;
}
