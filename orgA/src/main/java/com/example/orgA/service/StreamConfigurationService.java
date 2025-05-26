package com.example.orgA.service;

import com.example.orgA.dto.*;
import com.example.orgA.entity.*;
import com.example.orgA.repository.AttributeSettingRepository;
import com.example.orgA.repository.FilterConfigRepository;
import com.example.orgA.utils.AppConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StreamConfigurationService {

    @Autowired
    private FilterConfigRepository filterRepository;

    @Autowired
    private AttributeSettingRepository repository;

    @Autowired
    private ObjectMapper mapper;

    public AttributeSetting saveAttributes(AttributeSettingDTO dto) {
        AttributeSetting setting = new AttributeSetting();
        setting.setCaseId(dto.getCaseId());
        setting.setActivity(dto.getActivity());
        setting.setTimeStamp(dto.getTimeStamp());
        setting.setStatus(dto.getStatus() != null
                ? dto.getStatus()
                : AppConstants.STATUS_ACTIVE);
        return repository.save(setting);
    }

    public AttributeSetting getAttributeSettingById(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Update only the core fields + status.
     */
    public AttributeSetting updateAttributeSetting(Long id, AttributeSettingDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setCaseId(dto.getCaseId());
            existing.setActivity(dto.getActivity());
            existing.setTimeStamp(dto.getTimeStamp());
            if (dto.getStatus() != null) {
                existing.setStatus(dto.getStatus());
            }
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("AttributeSetting not found: " + id));
    }

    public boolean updateAttributeStatus(Long id, String status) {
        return repository.findById(id).map(setting -> {
            setting.setStatus(status);
            repository.save(setting);
            return true;
        }).orElse(false);
    }

    public boolean deleteAttributeSetting(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
    public FilterConfig saveFilter(FilterConfigDTO dto) {
        try {
            FilterConfig config = new FilterConfig();
            config.setFilters(mapper.writeValueAsString(dto.getFilters()));
            config.setStatus(dto.getStatus() != null ? dto.getStatus() : AppConstants.STATUS_ACTIVE);
            return filterRepository.save(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save filter config", e);
        }
    }
    public FilterConfig getFilterById(Long id) {
        System.out.println("In filter service");
        return filterRepository.findById(id)
                .orElse(null);
    }
    public FilterConfig updateFilter(Long id, FilterConfigDTO dto) {
        Optional<FilterConfig> optional = filterRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Filter config not found with id: " + id);
        }

        try {
            FilterConfig config = optional.get();
            config.setFilters(mapper.writeValueAsString(dto.getFilters()));
            if (dto.getStatus() != null) {
                config.setStatus(dto.getStatus());
            }
            return filterRepository.save(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to update filter config", e);
        }
    }
    public boolean updateFilterStatus(Long id, String status) {
        return filterRepository.findById(id).map(config -> {
            config.setStatus(status);
            filterRepository.save(config);
            return true;
        }).orElse(false);
    }
    public boolean deleteFilter(Long id) {
        if (filterRepository.existsById(id)) {
            filterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
