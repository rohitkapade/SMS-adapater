package com.tml.uep.service;

import com.tml.uep.model.dto.customerquery.CustomerQueryDTO;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.dto.customerquery.CustomerQueryUpdateRequest;
import com.tml.uep.model.kafka.IncomingMessage;

import java.time.OffsetDateTime;
import java.util.List;

public interface CustomerQueryService {
    boolean createCustomerQuery(IncomingMessage incomingMessage);

    boolean updateCustomerQuery(long queryId, CustomerQueryUpdateRequest updateRequest);

    List<CustomerQueryDTO> getCustomerQueries(OffsetDateTime fromDateTime,
                                              OffsetDateTime toDateTime,
                                              CustomerQueryStatus status,
                                              String assignedTo);


}
