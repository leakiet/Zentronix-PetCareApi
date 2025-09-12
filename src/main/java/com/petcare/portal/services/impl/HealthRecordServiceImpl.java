package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.petcare.portal.entities.HealthRecord;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.services.HealthRecordService;
import com.petcare.portal.repositories.HealthRecordRepository;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.dtos.healthRecordDto.CreateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.UpdateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.HealthRecordResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public HealthRecordResponse createHealthRecord(CreateHealthRecordRequest request) {
        try {
            // Find the pet
            Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));
            
            // Find the vet if vetId is provided
            User vet = null;
            if (request.getVetId() != null) {
                vet = userRepository.findById(request.getVetId())
                    .orElseThrow(() -> new RuntimeException("Vet not found with id: " + request.getVetId()));
            }
            
            // Create new health record
            HealthRecord healthRecord = new HealthRecord();
            healthRecord.setPet(pet);
            healthRecord.setVet(vet);
            healthRecord.setRecordType(request.getRecordType());
            healthRecord.setTitle(request.getTitle());
            healthRecord.setVisitDate(request.getVisitDate());
            healthRecord.setDiagnosis(request.getDiagnosis());
            healthRecord.setTreatment(request.getTreatment());
            healthRecord.setNotes(request.getNotes());
            healthRecord.setVetName(request.getVetName());
            healthRecord.setIsDeleted(false);
            
            HealthRecord savedRecord = healthRecordRepository.save(healthRecord);
            return convertToResponse(savedRecord);
        } catch (Exception e) {
            throw new RuntimeException("Error creating health record: " + e.getMessage(), e);
        }
    }

    @Override
    public HealthRecordResponse updateHealthRecord(Long id, UpdateHealthRecordRequest request) {
        try {
            HealthRecord existingRecord = healthRecordRepository.findByIdAndNotDeleted(id);
            if (existingRecord == null) {
                throw new RuntimeException("Health record not found with id: " + id);
            }
            
            // Find the vet if vetId is provided
            if (request.getVetId() != null) {
                User vet = userRepository.findById(request.getVetId())
                    .orElseThrow(() -> new RuntimeException("Vet not found with id: " + request.getVetId()));
                existingRecord.setVet(vet);
            }
            
            // Update fields if provided
            if (request.getRecordType() != null) {
                existingRecord.setRecordType(request.getRecordType());
            }
            if (request.getTitle() != null) {
                existingRecord.setTitle(request.getTitle());
            }
            if (request.getVisitDate() != null) {
                existingRecord.setVisitDate(request.getVisitDate());
            }
            if (request.getDiagnosis() != null) {
                existingRecord.setDiagnosis(request.getDiagnosis());
            }
            if (request.getTreatment() != null) {
                existingRecord.setTreatment(request.getTreatment());
            }
            if (request.getNotes() != null) {
                existingRecord.setNotes(request.getNotes());
            }
            if (request.getVetName() != null) {
                existingRecord.setVetName(request.getVetName());
            }
            if (request.getIsDeleted() != null) {
                existingRecord.setIsDeleted(request.getIsDeleted());
            }
            
            HealthRecord updatedRecord = healthRecordRepository.save(existingRecord);
            return convertToResponse(updatedRecord);
        } catch (Exception e) {
            throw new RuntimeException("Error updating health record: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteHealthRecord(Long id) {
        try {
            HealthRecord record = healthRecordRepository.findByIdAndNotDeleted(id);
            if (record == null) {
                throw new RuntimeException("Health record not found with id: " + id);
            }

            record.setIsDeleted(true);
            healthRecordRepository.save(record);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting health record: " + e.getMessage(), e);
        }
    }

    @Override
    public HealthRecordResponse getHealthRecordById(Long id) {
        try {
            HealthRecord record = healthRecordRepository.findByIdAndNotDeleted(id);
            if (record == null) {
                throw new RuntimeException("Health record not found with id: " + id);
            }
            return convertToResponse(record);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching health record: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HealthRecordResponse> getHealthRecordsByPetId(Long petId) {
        try {
            List<HealthRecord> records = healthRecordRepository.findByPetIdAndNotDeleted(petId);
            return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching health records for pet: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HealthRecordResponse> getAllHealthRecords() {
        try {
            List<HealthRecord> records = healthRecordRepository.findAllNotDeleted();
            return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all health records: " + e.getMessage(), e);
        }
    }

    private HealthRecordResponse convertToResponse(HealthRecord record) {
        HealthRecordResponse response = new HealthRecordResponse();
        response.setId(record.getId());
        response.setPetId(record.getPet().getId());
        response.setPetName(record.getPet().getPetName());
        response.setVetId(record.getVet() != null ? record.getVet().getId() : null);
        response.setVetName(record.getVetName());
        response.setRecordType(record.getRecordType());
        response.setTitle(record.getTitle());
        response.setVisitDate(record.getVisitDate());
        response.setDiagnosis(record.getDiagnosis());
        response.setTreatment(record.getTreatment());
        response.setNotes(record.getNotes());
        response.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().toLocalDate() : null);
        response.setUpdatedAt(record.getUpdatedAt() != null ? record.getUpdatedAt().toLocalDate() : null);
        return response;
    }
}
