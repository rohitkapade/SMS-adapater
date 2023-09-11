package com.tml.uep.model.entity;

import com.tml.uep.model.BusinessUnit;
import com.tml.uep.model.Event;
import com.tml.uep.model.EventSource;
import com.tml.uep.model.EventStatus;
import com.tml.uep.model.kafka.MessageType;
import com.tml.uep.model.kafka.OutboundEvent;
import com.vladmihalcea.hibernate.type.json.JsonType;
import java.time.OffsetDateTime;
import java.util.HashMap;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(
        name = "uep_file_events",
        indexes = {@Index(name = "fileEvent_status_index", columnList = "status")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@TypeDef(name = "json", typeClass = JsonType.class)
public class FileEvent {
    @Id private String uniqueId;

    private String phoneNumber;

    private OffsetDateTime creationDateTime;

    @Enumerated(EnumType.STRING)
    private Event eventName;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(columnDefinition = "TEXT")
    private String s3Url;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private HashMap<String, Object> payload;

    public OutboundEvent createOutBoundEvent(BusinessUnit businessUnit) {
        payload.put("fileUrl", s3Url);
        return new OutboundEvent(
                eventName,
                uniqueId,
                OffsetDateTime.now(),
                businessUnit,
                phoneNumber,
                payload,
                MessageType.DOCUMENT,
                s3Url,
                null,
                EventSource.SERVICE_NOTIFICATIONS);
    }
}
