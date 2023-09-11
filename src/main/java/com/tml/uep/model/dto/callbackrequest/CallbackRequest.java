package com.tml.uep.model.dto.callbackrequest;

import com.tml.uep.model.CallbackRequestStatus;
import java.time.OffsetDateTime;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackRequest {

    private Long id;

    @NotBlank private OffsetDateTime startDateTime;

    @NotBlank private OffsetDateTime endDateTime;

    @NotBlank private String mobileNumber;

    @NotBlank private String customerId;

    @Enumerated(EnumType.STRING)
    private CallbackRequestStatus callbackRequestStatus;

    private String assignedTo;

    @NotBlank private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
