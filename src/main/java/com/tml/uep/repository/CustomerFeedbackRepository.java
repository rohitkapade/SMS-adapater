package com.tml.uep.repository;

import com.tml.uep.model.Group;
import com.tml.uep.model.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqualAndGroupName(
            OffsetDateTime startDateTime, OffsetDateTime endDateTime, Group groupName);
}
