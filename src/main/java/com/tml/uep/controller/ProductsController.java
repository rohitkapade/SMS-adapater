package com.tml.uep.controller;

import com.tml.uep.model.dto.product.ProductResponse;
import com.tml.uep.service.ProductsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;

@Slf4j
@RestController
@Validated
@RequestMapping("/product")
@AllArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("/{mobileNumber}")
    public ResponseEntity<ProductResponse> getProductResponse(@PathVariable  @Size(min = 10, max = 10, message = "Mobile number cannot be of less than 10 digits") String mobileNumber) {

        log.info("Incoming request to product list endpoint for {}", mobileNumber);

        ProductResponse productResponse = productsService.getAllProducts(mobileNumber);

        return ResponseEntity.status(productResponse.getHttpStatus()).body(productResponse);
    }
}
