package com.petcare.portal.services;

import com.petcare.portal.dtos.healthRecordDto.CreateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.UpdateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.HealthRecordResponse;
import java.util.List;

public interface HealthRecordService {
    
    HealthRecordResponse createHealthRecord(CreateHealthRecordRequest request);
    
    HealthRecordResponse updateHealthRecord(Long id, UpdateHealthRecordRequest request);
    
    void deleteHealthRecord(Long id);
    
    HealthRecordResponse getHealthRecordById(Long id);
    
    List<HealthRecordResponse> getHealthRecordsByPetId(Long petId);
    
    List<HealthRecordResponse> getAllHealthRecords();
}
