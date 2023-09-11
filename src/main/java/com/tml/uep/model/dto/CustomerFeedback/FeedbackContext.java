package com.tml.uep.model.dto.CustomerFeedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackContext {
    String feedbackText;
    String feedbackTargetId;
    String feedbackTargetLabel;
}
