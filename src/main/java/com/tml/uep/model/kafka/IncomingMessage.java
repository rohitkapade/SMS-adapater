package com.tml.uep.model.kafka;

import com.tml.uep.model.Channel;
import com.tml.uep.model.RasaButton;
import com.tml.uep.model.Scenario;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class IncomingMessage {
    private String customerId;
    private LocalDateTime receivedAt;
    private String messageId;
    private String message;
    private String payload;
    private Scenario scenario;
    private String fileUrl;
    private MessageType messageType;
    private Double longitude;
    private Double latitude;
    private String locationName;
    private String address;
    private Channel channel;
    private List<RasaButton> buttons;
    private String data;

    private boolean isAuthenticated;

}
