package com.example.orgA.service;

import com.example.orgA.dto.MiningResultDTO;
import com.example.orgA.entity.MiningResult;
import com.example.orgA.repository.MiningResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MiningResultService {

    private final MiningResultRepository repository;

    public MiningResultService(MiningResultRepository repository) {
        this.repository = repository;
    }

    public MiningResult saveResult(MiningResultDTO dto) {
        MiningResult result = new MiningResult();

        result.setResult(dto.getResult());
        result.setCreatedAt(LocalDateTime.now());
        return repository.save(result);
    }
}
