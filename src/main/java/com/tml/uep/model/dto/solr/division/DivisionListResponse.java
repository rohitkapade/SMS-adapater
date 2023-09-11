package com.tml.uep.model.dto.solr.division;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DivisionListResponse {

    @JsonAlias("division_list")
    private List<DivisionDetails> divisionList;
}
