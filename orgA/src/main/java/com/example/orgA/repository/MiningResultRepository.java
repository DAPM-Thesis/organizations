package com.example.orgA.repository;

import com.example.orgA.entity.MiningResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiningResultRepository extends JpaRepository<MiningResult, Long> {
}
