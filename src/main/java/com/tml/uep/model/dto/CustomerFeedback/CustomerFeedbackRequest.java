package com.tml.uep.model.dto.CustomerFeedback;

import com.tml.uep.model.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFeedbackRequest {

    private String mobileNumber;

    @NotNull private Category category;

    @NotNull private FeedbackSentiment feedbackSentiment;

    @NotNull private Group groupName;

    @NotNull private boolean isUserDefined;

    private FeedbackContext feedbackContext;
}
