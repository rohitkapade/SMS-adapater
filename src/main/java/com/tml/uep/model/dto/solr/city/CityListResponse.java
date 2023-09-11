package com.tml.uep.model.dto.solr.city;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CityListResponse {

    @JsonAlias("city_list")
    private List<String> cityList;
}
