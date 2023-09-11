package com.tml.uep.service.impl;

import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.customerquery.CustomerQueryDTO;
import com.tml.uep.model.dto.customerquery.CustomerQueryRequest;
import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.dto.customerquery.CustomerQueryUpdateRequest;
import com.tml.uep.model.entity.CustomerQuery;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.repository.CustomerQueryRepository;
import com.tml.uep.service.CustomerQueryService;
import com.tml.uep.service.MessageProcessor;
import com.tml.uep.service.S3FileService;

import com.tml.uep.utils.Utils;
import com.tml.uep.validator.ConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerQueryServiceImpl implements CustomerQueryService, MessageProcessor {

    private static final String UNDERSCORE = "_";
    public static final String CUSTOMER_QUERY = "CUSTOMER_QUERY";
    public static final String FORWARD_SLASH = "/";

    @Autowired private CustomerQueryRepository customerQueryRepository;

    @Autowired private S3FileService s3FileService;

    @Autowired private Utils utils;

    @Value("${aws.s3.fleet-edge-bucket-name}")
    String customerQueryBucketName;

    @Autowired
    private ConstraintValidator validator;

    @Override
    public Scenario getScenario() {
        return Scenario.CUSTOMER_QUERY;
    }

    @Override
    public boolean processMessage(IncomingMessage incomingMessage) {

        return createCustomerQuery(incomingMessage);
    }

    @Override
    public boolean createCustomerQuery(IncomingMessage incomingMessage) {

        CustomerQuery customerQuery = getCustomerQueryEntity(incomingMessage);
        validator.validate(customerQuery);

        customerQueryRepository.save(customerQuery);
        return true;
    }


    private CustomerQuery getCustomerQueryEntity(IncomingMessage incomingMessage) {
        CustomerQueryRequest customerQueryRequest = utils.toType(incomingMessage.getData(), CustomerQueryRequest.class);
        CustomerQuery customerQuery = new CustomerQuery(customerQueryRequest, incomingMessage.getCustomerId());
        return customerQuery;
    }

    @Override
    public boolean updateCustomerQuery(long queryId, CustomerQueryUpdateRequest updateRequest) {
        return customerQueryRepository.findById(queryId)
                .map(customerQuery -> {
                    updateCustomerQueryEntity(customerQuery, updateRequest);
                    return true;
                })
                .orElse(false);
    }

    private void updateCustomerQueryEntity(CustomerQuery customerQuery, CustomerQueryUpdateRequest updateRequest) {
        customerQuery.setAssignedTo(updateRequest.getAssignedTo());
        customerQuery.setStatus(updateRequest.getStatus());
        customerQuery.setUpdatedBy(updateRequest.getUpdatedBy());
        customerQueryRepository.save(customerQuery);
    }

    @Override
    public List<CustomerQueryDTO> getCustomerQueries(OffsetDateTime fromDateTime,
                                                     OffsetDateTime toDateTime,
                                                     CustomerQueryStatus status,
                                                     String assignedTo) {


        Date start = fromDateTime != null ? new Date(fromDateTime.toInstant().toEpochMilli()) : null;
        Date end = toDateTime != null ? new Date(toDateTime.toInstant().toEpochMilli()) : null;
        return customerQueryRepository.getCustomerQueries(start, end, status, assignedTo)
                .stream()
                .map(CustomerQueryDTO::fromCustomerQueryEntity)
                .collect(Collectors.toList());
    }


}
