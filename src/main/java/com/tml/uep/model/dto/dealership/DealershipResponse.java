package com.tml.uep.model.dto.dealership;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DealershipResponse {

    private List<Dealership> dealerships;
    @JsonIgnore
    private HttpStatus httpStatus;
}
