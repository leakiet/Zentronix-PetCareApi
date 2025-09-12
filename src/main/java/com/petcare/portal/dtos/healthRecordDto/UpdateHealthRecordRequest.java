package com.petcare.portal.dtos.healthRecordDto;

import com.petcare.portal.enums.RecordType;
import java.time.LocalDate;

public class UpdateHealthRecordRequest {
    
    private Long vetId; // nullable
    private RecordType recordType;
    private String title;
    private LocalDate visitDate;
    private String diagnosis;
    private String treatment;
    private String notes;
    private String vetName;
    private Boolean isDeleted;
    
    // Constructors
    public UpdateHealthRecordRequest() {}
    
    public UpdateHealthRecordRequest(Long vetId, RecordType recordType, String title, 
                                   LocalDate visitDate, String diagnosis, String treatment, 
                                   String notes, String vetName, Boolean isDeleted) {
        this.vetId = vetId;
        this.recordType = recordType;
        this.title = title;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.vetName = vetName;
        this.isDeleted = isDeleted;
    }
    
    // Getters and Setters
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
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
