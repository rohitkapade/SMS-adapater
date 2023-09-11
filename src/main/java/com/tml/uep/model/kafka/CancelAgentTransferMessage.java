package com.tml.uep.model.kafka;

import com.tml.uep.model.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CancelAgentTransferMessage {

    private String customerId;
    private Channel channel;
}
