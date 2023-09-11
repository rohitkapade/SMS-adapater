package com.tml.uep.repository;

import com.tml.uep.model.dto.customerquery.CustomerQueryStatus;
import com.tml.uep.model.entity.CustomerQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface CustomerQueryRepository extends JpaRepository<CustomerQuery, Long> {

    @Query(value =
                    "SELECT c FROM CustomerQuery c where "
                            + "(cast(:fromDateTime as date) is null  or c.createdAt >= :fromDateTime) and "
                            + "(cast(:toDateTime as date) is null  or c.createdAt <= :toDateTime) and "
                            + "(:status is null or c.status = :status) and "
                            + "(:assignedTo is null or c.assignedTo = :assignedTo)"
    )
    List<CustomerQuery> getCustomerQueries(@Param("fromDateTime") Date fromDateTime,
                                           @Param("toDateTime") Date toDateTime,
                                           @Param("status") CustomerQueryStatus status,
                                           @Param("assignedTo") String assignedTo);
}
