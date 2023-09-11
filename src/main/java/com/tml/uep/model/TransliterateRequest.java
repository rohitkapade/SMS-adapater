package com.tml.uep.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransliterateRequest {
    private List<String> data;
    private Boolean isBulk;
    private Boolean ignoreTaggedEntities;

    public TransliterateRequest(CvOptyDetailsRequest request) {
        this.data = List.of(request.getFirstName(), request.getLastName());
        this.isBulk = false;
        this.ignoreTaggedEntities = false;
    }
}
