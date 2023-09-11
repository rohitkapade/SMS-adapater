package com.tml.uep.service;

import com.tml.uep.model.dto.product.ProductResponse;

public interface ProductsService {

    ProductResponse getAllProducts(String mobileNumber);
}
