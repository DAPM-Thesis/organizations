package com.dapm2.ingestion_service.repository;

import com.dapm2.ingestion_service.entity.AttributeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeSettingRepository extends JpaRepository<AttributeSetting, Long> {
}
