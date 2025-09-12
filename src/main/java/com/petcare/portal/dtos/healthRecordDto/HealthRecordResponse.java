package com.petcare.portal.dtos.healthRecordDto;

import com.petcare.portal.enums.RecordType;
import java.time.LocalDate;

public class HealthRecordResponse {
    
    private Long id;
    private Long petId;
    private String petName;
    private Long vetId;
    private String vetName;
    private RecordType recordType;
    private String title;
    private LocalDate visitDate;
    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    // Constructors
    public HealthRecordResponse() {}
    
    public HealthRecordResponse(Long id, Long petId, String petName, Long vetId, String vetName,
                              RecordType recordType, String title, LocalDate visitDate, 
                              String diagnosis, String treatment, String notes,
                              LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.petId = petId;
        this.petName = petName;
        this.vetId = vetId;
        this.vetName = vetName;
        this.recordType = recordType;
        this.title = title;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPetId() {
        return petId;
    }
    
    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    public String getPetName() {
        return petName;
    }
    
    public void setPetName(String petName) {
        this.petName = petName;
    }
    
    public Long getVetId() {
        return vetId;
    }
    
    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }
    
    public String getVetName() {
        return vetName;
    }
    
    public void setVetName(String vetName) {
        this.vetName = vetName;
    }
    
    public RecordType getRecordType() {
        return recordType;
    }
    
    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public LocalDate getVisitDate() {
        return visitDate;
    }
    
    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}
