package com.tml.uep.model.dto.cbslconfcall;

import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.entity.CbslConfCallEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CbslConfCallDetails {

    private String sid;

    private String fromPhoneNumber;

    private OffsetDateTime initiatedStartTime;

    private CbslConfCallStatus status;

    private String dialCallDuration;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private String recordingUrl;

    public CbslConfCallDetails(CbslConfCallEntity entity) {
        this.sid = entity.getSid();
        this.fromPhoneNumber = entity.getFromPhoneNumber();
        this.initiatedStartTime = entity.getInitiatedStartTime();
        this.status = entity.getStatus();
        this.dialCallDuration = entity.getDialCallDuration();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        this.recordingUrl = entity.getRecordingUrl();
    }
}
