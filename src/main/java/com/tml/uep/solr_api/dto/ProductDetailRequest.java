package com.tml.uep.solr_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRequest {
    private String entity;
    private String lob;
    private String ppl;
}
