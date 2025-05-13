package com.dapm2.ingestion_service.entity;

import com.dapm2.ingestion_service.utils.AppConstants;
import com.dapm2.ingestion_service.utils.StringListToJsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "anonymization_rules")
@Getter
@Setter
public class AnonymizationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_source_id", nullable = false, unique = true)
    private String dataSourceId;

    @Column(name = "pseudonymization", columnDefinition = "TEXT")
    @Convert(converter = StringListToJsonConverter.class)
    private List<String> pseudonymization;

    @Column(name = "suppression", columnDefinition = "TEXT")
    @Convert(converter = StringListToJsonConverter.class)
    private List<String> suppression;

    @Column(nullable = false)
    private String status = AppConstants.STATUS_ACTIVE;

    @Column(name = "unique_field", nullable = false)
    private String uniqueField;
}
