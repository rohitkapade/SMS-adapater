package com.tml.uep.repository;

import com.tml.uep.model.entity.ProductLineVcMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLineVcMappingRepository extends JpaRepository<ProductLineVcMapping, Long> {
    void deleteByPpl(String ppl);
}
