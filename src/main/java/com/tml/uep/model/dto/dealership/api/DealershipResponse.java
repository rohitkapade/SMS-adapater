package com.tml.uep.model.dto.dealership.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class DealershipResponse {

    private List<Dealership> dealers;
    @JsonIgnore
    private HttpStatus httpStatus;

    public DealershipResponse(HttpStatus httpStatus){
        this.httpStatus = httpStatus;
    }
}
