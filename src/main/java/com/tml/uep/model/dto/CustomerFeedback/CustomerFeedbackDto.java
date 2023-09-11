package com.tml.uep.model.dto.CustomerFeedback;

import com.tml.uep.model.Group;
import com.tml.uep.model.entity.CustomerFeedback;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerFeedbackDto {
    private Long feedbackId;

    private String mobileNumber;

    private Category category;

    private FeedbackSentiment feedbackSentiment;

    private Group groupName;

    private boolean isUserDefined;

    private OffsetDateTime createdAt;

    private FeedbackContext feedbackContext;

    public CustomerFeedbackDto(CustomerFeedback customerFeedback) {
        this.feedbackId = customerFeedback.getFeedbackId();
        this.mobileNumber = customerFeedback.getMobileNumber();
        this.category = customerFeedback.getCategory();
        this.feedbackSentiment = customerFeedback.getFeedbackSentiment();
        this.groupName = customerFeedback.getGroupName();
        this.createdAt = customerFeedback.getCreatedAt();
        this.feedbackContext = customerFeedback.getFeedbackContext();
        this.isUserDefined = customerFeedback.getIsUserDefined();
    }
}
