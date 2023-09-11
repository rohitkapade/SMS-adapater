package com.tml.uep.service;

import com.tml.uep.model.dto.dealership.DealershipResponse;

public interface DealershipService {

    DealershipResponse getAllDealers(String mobileNumber);
}
