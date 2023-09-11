package com.tml.uep.model.dto.callbackrequest;

import com.tml.uep.model.CallbackRequestStatus;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackUpdateRequest {

    @Enumerated(EnumType.STRING)
    @NotNull
    private CallbackRequestStatus callbackRequestStatus;

    @NotBlank
    private String updatedBy;

    @NotBlank
    private String assignedTo;
}
