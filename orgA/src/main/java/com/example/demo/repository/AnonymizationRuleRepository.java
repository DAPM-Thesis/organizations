package com.example.demo.repository;

import com.example.demo.entity.AnonymizationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnonymizationRuleRepository extends JpaRepository<AnonymizationRule, Long> {
    Optional<AnonymizationRule> findFirstByDataSourceId(String dataSourceId);
}
