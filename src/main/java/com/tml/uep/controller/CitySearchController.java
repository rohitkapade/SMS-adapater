package com.tml.uep.controller;

import com.tml.uep.model.dto.solr.city.CityListRequest;
import com.tml.uep.model.dto.solr.city.CityListResponse;
import com.tml.uep.service.CitySearchService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("city-search")
@Slf4j
public class CitySearchController {

    @Autowired private CitySearchService citySearchService;

    @PostMapping("")
    public ResponseEntity searchCityList(@Valid @RequestBody CityListRequest request) {
        log.info("Calling city list API for state: {}", request.getState());
        CityListResponse cityList = citySearchService.fetchCityList(request);
        return ResponseEntity.ok(cityList);
    }
}
