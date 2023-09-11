package com.tml.uep.model;

import com.tml.uep.model.kafka.OutboundEvent;

public interface OutboundEventConverter {

    OutboundEvent convertToOutboundEvent(BusinessUnit businessUnit);

    String getId();

    default boolean isValid() {
        return true;
    }
}
