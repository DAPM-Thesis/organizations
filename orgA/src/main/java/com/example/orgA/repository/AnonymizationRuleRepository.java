package com.example.orgA.repository;

import com.example.orgA.entity.AnonymizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnonymizationRuleRepository extends JpaRepository<AnonymizationRule, Long> {
    Optional<AnonymizationRule> findFirstByDataSourceId(String dataSourceId);
}
