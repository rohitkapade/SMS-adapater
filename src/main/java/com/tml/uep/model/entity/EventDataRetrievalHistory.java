package com.tml.uep.model.entity;

import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uep_event_data_retrieval_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EventDataRetrievalHistory {
    @Id private String eventName;

    private OffsetDateTime dateTime;
}
