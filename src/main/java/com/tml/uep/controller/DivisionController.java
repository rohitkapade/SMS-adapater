package com.tml.uep.controller;

import com.tml.uep.model.dto.solr.division.DivisionListRequest;
import com.tml.uep.model.dto.solr.division.DivisionListResponse;
import com.tml.uep.service.DivisionService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("division-search")
@Slf4j
public class DivisionController {

    @Autowired private DivisionService divisionService;

    @PostMapping("")
    public ResponseEntity searchDivisionList(@Valid @RequestBody DivisionListRequest request) {
        log.info(
                "Calling division list API for state: {} and city : {}",
                request.getState(),
                request.getCity());
        DivisionListResponse response = divisionService.fetchDivisionList(request);
        return ResponseEntity.ok(response);
    }
}
