package com.tml.uep.controller;

import com.tml.uep.model.ProductLine;
import com.tml.uep.service.ProductService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("product-lines")
public class ProductController {

    @Autowired private ProductService productService;

    @GetMapping()
    public List<ProductLine> getAllProductLines() {
        return productService.getAllProductLines();
    }

    @PostMapping("/sync")
    public ResponseEntity syncProductLines() {
        productService.syncProductLines();
        return ResponseEntity.ok().build();
    }
}
