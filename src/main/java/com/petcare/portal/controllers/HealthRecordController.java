package com.petcare.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.petcare.portal.services.HealthRecordService;
import com.petcare.portal.dtos.healthRecordDto.CreateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.UpdateHealthRecordRequest;
import com.petcare.portal.dtos.healthRecordDto.HealthRecordResponse;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apis/v1/health-records")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @PostMapping("/create")
    public ResponseEntity<HealthRecordResponse> createHealthRecord(@Valid @RequestBody CreateHealthRecordRequest request) {
        try {
            HealthRecordResponse response = healthRecordService.createHealthRecord(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<HealthRecordResponse> updateHealthRecord(
            @PathVariable("id") Long id, 
            @Valid @RequestBody UpdateHealthRecordRequest request) {
        try {
            HealthRecordResponse response = healthRecordService.updateHealthRecord(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<String> deleteHealthRecord(@PathVariable("id") Long id) {
        try {
            healthRecordService.deleteHealthRecord(id);
            return ResponseEntity.ok("Health record deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthRecordResponse> getHealthRecordById(@PathVariable("id") Long id) {
        try {
            HealthRecordResponse response = healthRecordService.getHealthRecordById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<HealthRecordResponse>> getHealthRecordsByPetId(@PathVariable("petId") Long petId) {
        try {
            List<HealthRecordResponse> records = healthRecordService.getHealthRecordsByPetId(petId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<HealthRecordResponse>> getAllHealthRecords() {
        try {
            List<HealthRecordResponse> records = healthRecordService.getAllHealthRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
