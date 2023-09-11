package com.tml.uep.service;

import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.Scenario;
import com.tml.uep.model.dto.callbackrequest.CallbackCreationRequest;
import com.tml.uep.model.dto.callbackrequest.CallbackRequest;
import com.tml.uep.model.dto.callbackrequest.CallbackUpdateRequest;
import com.tml.uep.model.entity.CallbackRequestEntity;
import com.tml.uep.model.kafka.IncomingMessage;
import com.tml.uep.repository.CallbackRequestRepository;
import com.tml.uep.utils.Utils;
import com.tml.uep.validator.ConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CallbackRequestService implements MessageProcessor {

    @Autowired private Utils utils;
    @Autowired private CallbackRequestRepository repository;

    @Autowired private ConstraintValidator validator;

    @Override
    public Scenario getScenario() {
        return Scenario.CALLBACK_REQUEST;
    }

    @Override
    public boolean processMessage(IncomingMessage incomingMessage) {
        return createCallbackRequest(incomingMessage);
    }

    private boolean createCallbackRequest(IncomingMessage incomingMessage) {

        CallbackRequestEntity callbackRequestEntity = getCallbackRequestEntity(incomingMessage);
        validator.validate(callbackRequestEntity);

        repository.save(callbackRequestEntity);
        return true;
    }


    private CallbackRequestEntity getCallbackRequestEntity(IncomingMessage incomingMessage) {
        CallbackCreationRequest callbackCreationRequest = utils.toType(incomingMessage.getData(), CallbackCreationRequest.class);
        CallbackRequestEntity callbackRequestEntity = new CallbackRequestEntity(callbackCreationRequest, incomingMessage.getCustomerId());
        return callbackRequestEntity;
    }

    public boolean updateCallbackRequest(
            CallbackUpdateRequest callbackUpdateRequest, Long callbackId) {
        Optional<CallbackRequestEntity> callbackOptional = repository.findById(callbackId);
        if (callbackOptional.isEmpty()) {
            return false;
        }
        CallbackRequestEntity callbackRequestEntity = callbackOptional.get();
        callbackRequestEntity.setUpdatedBy(callbackUpdateRequest.getUpdatedBy());
        callbackRequestEntity.setAssignedTo(callbackUpdateRequest.getAssignedTo());
        callbackRequestEntity.setCallbackRequestStatus(
                callbackUpdateRequest.getCallbackRequestStatus());

        repository.save(callbackRequestEntity);
        return true;
    }

    public List<CallbackRequest> getCallbackRequests(
            OffsetDateTime startDateTime, OffsetDateTime endDateTime, CallbackRequestStatus status, String assignedTo) {

        List<CallbackRequestEntity> callbackRequestEntityList =
                repository.getCallbackRequests(
                        startDateTime, endDateTime, status, assignedTo);

        return callbackRequestEntityList.stream()
                .map(callbackRequestEntity -> callbackRequestEntity.toCallbackRequest())
                .collect(Collectors.toList());
    }


}
