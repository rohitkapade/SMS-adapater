package com.tml.uep.repository;

import com.tml.uep.model.CallbackRequestStatus;
import com.tml.uep.model.entity.CallbackRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CallbackRequestRepository extends JpaRepository<CallbackRequestEntity, Long> {

    @Query(value =
            "SELECT c FROM CallbackRequestEntity c where "
                    + "(cast(:fromDateTime as date) is null  or c.startDateTime >= :fromDateTime) and "
                    + "(cast(:toDateTime as date) is null  or c.endDateTime <= :toDateTime) and "
                    + "(:status is null or c.callbackRequestStatus = :status) and "
                    + "(:assignedTo is null or c.assignedTo = :assignedTo)")
    List<CallbackRequestEntity> getCallbackRequests(@Param("fromDateTime") OffsetDateTime fromDateTime,
                                                    @Param("toDateTime") OffsetDateTime toDateTime,
                                                    @Param("status") CallbackRequestStatus status,
                                                    @Param("assignedTo") String assignedTo);
}
