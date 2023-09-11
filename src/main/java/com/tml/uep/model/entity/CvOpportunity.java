package com.tml.uep.model.entity;

import com.tml.uep.model.CvOptyCreationRequest;
import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Getter
@Entity
@Table(name = "uep_cv_opportunities")
@NoArgsConstructor
@AllArgsConstructor
public class CvOpportunity {

    @Id
    @SequenceGenerator(
            name = "opportunity_id_primary_key_sequence",
            sequenceName = "opportunity_id_primary_key_sequence",
            allocationSize = 1)
    @GeneratedValue(generator = "opportunity_id_primary_key_sequence")
    private Long id;

    private String phoneNumber;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private CvOptyCreationRequest request;

    private String opportunityId;

    private OffsetDateTime dateTime;

    public CvOpportunity(CvOptyCreationRequest request, String opportunityId) {
        this.phoneNumber = request.getContact().getMobileNumber();
        this.request = request;
        this.opportunityId = opportunityId;
        this.dateTime = OffsetDateTime.now();
    }
}
