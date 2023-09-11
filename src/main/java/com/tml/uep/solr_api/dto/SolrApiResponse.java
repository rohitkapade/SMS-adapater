package com.tml.uep.solr_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SolrApiResponse<T> {

    /*
     * This holds the total count of records in Solr collection for requested event.
     * Record count for current API response can be obtained by counting records obtained by
     * parsing the actual json response.
     * */
    @JsonProperty("total_count")
    private int totalRecordCount;

    private List<T> data;

    @Override
    public String toString() {
        return "SolrApiResponse{" + "totalRecordCount=" + totalRecordCount + ", data=" + data + '}';
    }
}
