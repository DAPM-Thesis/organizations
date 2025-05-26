package com.example.orgA.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mining_table")
@Getter
@Setter
public class MiningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "result", columnDefinition = "TEXT", nullable = false)
    private String result;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
