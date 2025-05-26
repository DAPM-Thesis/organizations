package com.example.orgA.entity;

import com.example.orgA.utils.AppConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "filter_config")
@Getter
@Setter
public class FilterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String filters;

    @Column(nullable = false)
    private String status = AppConstants.STATUS_ACTIVE;
}
