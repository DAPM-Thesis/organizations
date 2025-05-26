package com.example.orgA.entity;

import com.example.orgA.utils.AppConstants;
import com.example.orgA.utils.StringListToJsonConverter;
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
