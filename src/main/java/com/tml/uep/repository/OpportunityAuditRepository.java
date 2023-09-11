package com.tml.uep.repository;

import com.tml.uep.model.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityAuditRepository extends JpaRepository<Opportunity, String> {}
