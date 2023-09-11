package com.tml.uep.service;

import com.tml.uep.model.CustomerDetailsResponse;
import com.tml.uep.solr_api.CustomerSolrService;
import com.tml.uep.solr_api.dto.CustomerSolrResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired private CustomerSolrService customerSolrService;

    public Optional<CustomerDetailsResponse> getCustomerDetails(String phoneNumber) {
        List<CustomerSolrResponse> customerDetails =
                customerSolrService.getCustomerDetails(phoneNumber);
        if (customerDetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new CustomerDetailsResponse(customerDetails.get(0)));
    }
}
