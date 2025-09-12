package com.petcare.portal.dtos.healthRecordDto;

import com.petcare.portal.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateHealthRecordRequest {
    
    @NotNull(message = "Pet ID is required")
    private Long petId;
    
    private Long vetId; // nullable
    
    @NotNull(message = "Record type is required")
    private RecordType recordType;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;
    
    private String diagnosis;
    private String treatment;
    private String notes;
    private String vetName;
    
    // Constructors
    public CreateHealthRecordRequest() {}
    
    public CreateHealthRecordRequest(Long petId, Long vetId, RecordType recordType, String title, 
                                   LocalDate visitDate, String diagnosis, String treatment, 
                                   String notes, String vetName) {
        this.petId = petId;
        this.vetId = vetId;
        this.recordType = recordType;
        this.title = title;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.vetName = vetName;
    }
    
    // Getters and Setters
    public Long getPetId() {
        return petId;
    }
    
    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    public Long getVetId() {
        return vetId;
    }
    
    public void setVetId(Long vetId) {
        this.vetId = vetId;
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
    
    public String getVetName() {
        return vetName;
    }
    
    public void setVetName(String vetName) {
        this.vetName = vetName;
    }
}
