package com.example.orgA.service;

import com.example.orgA.dto.AnonymizationRuleDTO;
import com.example.orgA.entity.AnonymizationRule;
import com.example.orgA.repository.AnonymizationRuleRepository;
import com.example.orgA.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnonymizationRuleService {

    @Autowired
    private AnonymizationRuleRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;
    public AnonymizationRule saveRule(AnonymizationRuleDTO request) {
        AnonymizationRule rule = new AnonymizationRule();
        rule.setDataSourceId(request.getDataSourceId());
        rule.setPseudonymization(request.getPseudonymization());
        rule.setSuppression(request.getSuppression());
        rule.setUniqueField(request.getUniqueField());
        rule.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : AppConstants.STATUS_ACTIVE
        );
        return repository.save(rule);
    }

    public AnonymizationRule getRuleById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public AnonymizationRule getRuleByDataSourceId(String dataSourceId) {
        return repository.findFirstByDataSourceId(dataSourceId).orElse(null);
    }

    public List<AnonymizationRule> getAllRules() {
        return repository.findAll();
    }

    public AnonymizationRule updateRule(Long id, AnonymizationRuleDTO request) {
        Optional<AnonymizationRule> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Anonymization rule not found with id: " + id);
        }
        AnonymizationRule rule = optional.get();
        rule.setDataSourceId(request.getDataSourceId());
        rule.setPseudonymization(request.getPseudonymization());
        rule.setSuppression(request.getSuppression());
        rule.setUniqueField(request.getUniqueField());
        if (request.getStatus() != null) {
            rule.setStatus(request.getStatus());
        }
        return repository.save(rule);
    }

    public boolean updateRuleStatus(Long id, String status) {
        return repository.findById(id)
                .map(r -> {
                    r.setStatus(status);
                    repository.save(r);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteRule(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}