package com.tml.uep.model.entity;

import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditable<U> {

    @CreatedDate
    @Temporal(TIMESTAMP)
    protected Date createdAt;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    protected Date updatedAt;

    protected String updatedBy;
}
