package com.tml.uep.repository;

import com.tml.uep.model.CbslConfCallStatus;
import com.tml.uep.model.entity.CbslConfCallEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CbslConfCallResponseRepository extends JpaRepository<CbslConfCallEntity, String> {

    @Query(
            value =
                    "SELECT c FROM CbslConfCallEntity c where "
                            + "(cast(:startTime as date) is null  or c.startTime >= :startTime) and "
                            + "(cast(:endTime as date) is null  or c.endTime <= :endTime) and "
                            + "(:status is null or c.status = :status)")
    List<CbslConfCallEntity> findAllByStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndStatus(
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("status") CbslConfCallStatus status);
}
