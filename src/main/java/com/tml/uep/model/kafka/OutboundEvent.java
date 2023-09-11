package com.tml.uep.model.kafka;

import com.tml.uep.model.BusinessUnit;
import com.tml.uep.model.Channel;
import com.tml.uep.model.Event;
import com.tml.uep.model.EventSource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OutboundEvent {
    private Event eventType;
    private String eventId;
    private OffsetDateTime eventDateTime;
    private BusinessUnit businessUnit;
    private String receiverId;
    private HashMap<String, Object> payload;
    private MessageType messageType;
    private String fileUrl;
    private Channel channel;
    private EventSource eventSource;

    public OutboundEvent(
            Event eventType,
            String eventId,
            OffsetDateTime eventDateTime,
            BusinessUnit businessUnit,
            String receiverId,
            HashMap<String, Object> payload) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.eventDateTime = eventDateTime;
        this.businessUnit = businessUnit;
        this.receiverId = receiverId;
        this.payload = payload;
        this.messageType = MessageType.TEXT;
        this.fileUrl = null;
        this.channel = null;
        this.eventSource = EventSource.SERVICE_NOTIFICATIONS;
    }

    public OutboundEvent(
            Event eventType,
            String eventId,
            OffsetDateTime eventDateTime,
            BusinessUnit businessUnit,
            String receiverId,
            String fileUrl,
            HashMap<String, Object> payload) {
        this(eventType, eventId, eventDateTime, businessUnit, receiverId, payload);
        this.fileUrl = fileUrl;
    }
}
