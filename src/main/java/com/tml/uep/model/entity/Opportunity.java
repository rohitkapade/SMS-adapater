package com.tml.uep.model.entity;

import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uep_opportunities")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Opportunity {
    @Id private String optyId;
    private String phoneNumber;
    private String conversationId;
    private OffsetDateTime optyCreationDateTime;
}
