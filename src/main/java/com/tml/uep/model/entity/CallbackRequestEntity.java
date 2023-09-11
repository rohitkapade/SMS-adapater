package com.tml.uep.model.entity;

import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.dto.callbackrequest.CallbackCreationRequest;
import com.tml.uep.model.dto.callbackrequest.CallbackRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;

@Data
@Table(name = "uep_callback_request")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CallbackRequestEntity extends Auditable<String>{

    @Id
    @SequenceGenerator(
            name = "uep_callback_request_id_primary_key_sequence",
            sequenceName = "uep_callback_request_id_primary_key_sequence",
            allocationSize = 1)
    @GeneratedValue(generator = "uep_callback_request_id_primary_key_sequence")
    private Long id;

    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;
    @NotBlank
    private String mobileNumber;
    @NotBlank
    private String customerId;
    private String assignedTo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CallbackRequestStatus callbackRequestStatus;

    public CallbackRequestEntity(CallbackCreationRequest callbackCreationRequest, String customerId) {
        this.startDateTime = callbackCreationRequest.getStartDateTime();
        this.endDateTime = callbackCreationRequest.getEndDateTime();
        this.mobileNumber = callbackCreationRequest.getMobileNumber();
        this.customerId = customerId;
        this.callbackRequestStatus = CallbackRequestStatus.NOT_STARTED;

    }

    public CallbackRequestEntity(OffsetDateTime startDateTime, OffsetDateTime endDateTime, String mobileNumber, String customerId, String assignedTo, CallbackRequestStatus callbackRequestStatus) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.mobileNumber = mobileNumber;
        this.customerId = customerId;
        this.assignedTo = assignedTo;
        this.callbackRequestStatus = callbackRequestStatus;
    }

    public CallbackRequest toCallbackRequest() {
        OffsetDateTime createdAt = this.createdAt != null ? OffsetDateTime.ofInstant(this.createdAt.toInstant(), ZoneOffset.UTC) : null;
        OffsetDateTime updatedAt = this.updatedAt != null ? OffsetDateTime.ofInstant(this.updatedAt.toInstant(), ZoneOffset.UTC) : null;
        return new CallbackRequest(
                this.id,
                this.startDateTime,
                this.endDateTime,
                this.mobileNumber,
                this.customerId,
                this.callbackRequestStatus,
                this.assignedTo,
                createdAt,
                updatedAt,
                this.updatedBy);
    }
}
