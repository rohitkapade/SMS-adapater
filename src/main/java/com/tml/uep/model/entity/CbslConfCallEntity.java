package com.tml.uep.model.entity;

import com.tml.uep.model.Call;
import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.dto.cbslconfcall.ConfCallRecordingRequest;
import com.tml.uep.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "uep_cbsl_conf_call_response")
@NoArgsConstructor
@AllArgsConstructor
public class CbslConfCallEntity {

    @Id private String sid;

    private String fromPhoneNumber;

    private OffsetDateTime initiatedStartTime;

    @Enumerated(EnumType.STRING)
    private CbslConfCallStatus status;

    private String dialCallDuration;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private String recordingUrl;

    public CbslConfCallEntity(Call response) {
        this.sid = response.getSid();
        this.initiatedStartTime =
                DateUtils.convertToOffsetDateTimeFromISTString(response.getStartTime());
        this.fromPhoneNumber = response.getFrom();
        this.status = CbslConfCallStatus.INITIATED;
    }

    public CbslConfCallEntity(ConfCallRecordingRequest request, CbslConfCallStatus status) {
        this.sid = request.getSid();
        this.fromPhoneNumber = request.getCallFrom();
        this.initiatedStartTime = null;
        this.status = status;
        this.dialCallDuration = request.getDialCallDuration();
        this.startTime = DateUtils.convertToOffsetDateTimeFromISTString(request.getStartTime());
        this.endTime = DateUtils.convertToOffsetDateTimeFromISTString(request.getEndTime());
        this.recordingUrl = request.getRecordingUrl();
    }

    public void updateConfCallEntityFromRecordingRequest(ConfCallRecordingRequest request) {
        this.status = CbslConfCallStatus.RECORDING_AVAILABLE;
        this.dialCallDuration = request.getDialCallDuration();
        this.startTime = DateUtils.convertToOffsetDateTimeFromISTString(request.getStartTime());
        this.endTime = DateUtils.convertToOffsetDateTimeFromISTString(request.getEndTime());
        this.recordingUrl = request.getRecordingUrl();
    }
}
