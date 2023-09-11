package com.tml.uep.repository;

import com.tml.uep.model.entity.CvOpportunity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CvOpportunityRepository extends JpaRepository<CvOpportunity, String> {
    Optional<List<CvOpportunity>> findAllByDateTimeBetween(
            @Param("startTime") OffsetDateTime startTime, @Param("endTime") OffsetDateTime endTime);
}
