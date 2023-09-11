package com.tml.uep.model.dto.product.api;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty
    private HttpStatus httpStatus;

    public ProductResponse(HttpStatus httpStatus)
    {
        this.httpStatus = httpStatus;
    }
}
