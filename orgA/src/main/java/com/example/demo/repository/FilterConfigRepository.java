package com.dapm2.ingestion_service.repository;

import com.dapm2.ingestion_service.entity.FilterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterConfigRepository extends JpaRepository<FilterConfig, Long> {}
