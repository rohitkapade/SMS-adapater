package com.tml.uep.model.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ProductResponse {

    private List<Product> products;
    @JsonIgnore
    private HttpStatus httpStatus;
}
