package com.tml.uep.repository;

import com.tml.uep.model.entity.EventDataRetrievalHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDataRetrievalHistoryRepository
        extends CrudRepository<EventDataRetrievalHistory, String> {}
