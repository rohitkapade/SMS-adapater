package com.tml.uep.model.dto.callbackrequest;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackCreationRequest {

    @NotNull private OffsetDateTime startDateTime;

    @NotNull private OffsetDateTime endDateTime;

    @NotBlank private String mobileNumber;

}
