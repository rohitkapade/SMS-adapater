package com.tml.uep.model.entity;

import com.tml.uep.model.Group;
import com.tml.uep.model.dto.CustomerFeedback.CustomerFeedbackRequest;
import com.tml.uep.model.dto.CustomerFeedback.FeedbackSentiment;
import com.tml.uep.model.dto.CustomerFeedback.FeedbackContext;
import com.tml.uep.model.dto.CustomerFeedback.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_feedback")
public class CustomerFeedback {

    @Id
    @SequenceGenerator(
            name = "customer_feedback_feedback_id_seq",
            sequenceName = "customer_feedback_feedback_id_seq",
            allocationSize = 1)
    @GeneratedValue(generator = "customer_feedback_feedback_id_seq")
    private Long feedbackId;

    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private FeedbackSentiment feedbackSentiment;

    @Enumerated(EnumType.STRING)
    private Group groupName;

    private Boolean isUserDefined;

    private OffsetDateTime createdAt;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private FeedbackContext feedbackContext;

    public CustomerFeedback(CustomerFeedbackRequest customerFeedback) {
        this.mobileNumber = customerFeedback.getMobileNumber();
        this.category = customerFeedback.getCategory();
        this.feedbackSentiment = customerFeedback.getFeedbackSentiment();
        this.groupName = customerFeedback.getGroupName();
        this.createdAt = OffsetDateTime.now();
        this.feedbackContext = customerFeedback.getFeedbackContext();
        this.isUserDefined = customerFeedback.isUserDefined();
    }
}
