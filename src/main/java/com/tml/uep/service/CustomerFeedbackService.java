package com.tml.uep.service;

import com.tml.uep.model.Group;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackDto;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackRequest;
import com.tml.uep.model.entity.CustomerFeedback;
import com.tml.uep.repository.CustomerFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerFeedbackService {

    @Autowired private CustomerFeedbackRepository feedbackRepository;

    public void saveCustomerFeedback(CustomerFeedbackRequest customerFeedbackRequest) {
        CustomerFeedback customerFeedback = new CustomerFeedback(customerFeedbackRequest);
        feedbackRepository.save(customerFeedback);
    }

    public List<CustomerFeedbackDto> fetchCustomerFeedback(
            OffsetDateTime startDateTime, OffsetDateTime endDateTime, Group groupName) {
        return feedbackRepository
                .findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqualAndGroupName(
                        startDateTime, endDateTime, groupName)
                .stream()
                .map(CustomerFeedbackDto::new)
                .collect(Collectors.toList());
    }
}
