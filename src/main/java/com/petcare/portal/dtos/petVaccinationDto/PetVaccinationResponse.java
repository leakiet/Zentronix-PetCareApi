package com.petcare.portal.dtos.petVaccinationDto;

import com.petcare.portal.enums.VaccineType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PetVaccinationResponse {
    
    private Long id;
    private Long petId;
    private String petName;
    private Long vetId;
    private VaccineType vaccineType;
    private String vaccineName;
    private LocalDate vaccinationDate;
    private LocalDate nextDueDate;
    private String notes;
    private String vetName;
    private boolean reminderEnabled;
    private boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long daysUntilDue; // calculated field
    private String status; // calculated field: "OVERDUE", "DUE_SOON", "UPCOMING", "COMPLETED"
    
    // Constructors
    public PetVaccinationResponse() {}
    
    public PetVaccinationResponse(Long id, Long petId, String petName, Long vetId, 
                                VaccineType vaccineType, String vaccineName, 
                                LocalDate vaccinationDate, LocalDate nextDueDate, 
                                String notes, String vetName, boolean reminderEnabled,
                                boolean isCompleted, LocalDateTime createdAt, 
                                LocalDateTime updatedAt) {
        this.id = id;
        this.petId = petId;
        this.petName = petName;
        this.vetId = vetId;
        this.vaccineType = vaccineType;
        this.vaccineName = vaccineName;
        this.vaccinationDate = vaccinationDate;
        this.nextDueDate = nextDueDate;
        this.notes = notes;
        this.vetName = vetName;
        this.reminderEnabled = reminderEnabled;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        calculateDaysUntilDue();
        calculateStatus();
    }
    
    private void calculateDaysUntilDue() {
        if (nextDueDate != null) {
            this.daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextDueDate);
        }
    }
    
    private void calculateStatus() {
        if (isCompleted) {
            this.status = "COMPLETED";
        } else if (daysUntilDue < 0) {
            this.status = "OVERDUE";
        } else if (daysUntilDue <= 7) {
            this.status = "DUE_SOON";
        } else {
            this.status = "UPCOMING";
        }
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
    
    public VaccineType getVaccineType() {
        return vaccineType;
    }
    
    public void setVaccineType(VaccineType vaccineType) {
        this.vaccineType = vaccineType;
    }
    
    public String getVaccineName() {
        return vaccineName;
    }
    
    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }
    
    public LocalDate getVaccinationDate() {
        return vaccinationDate;
    }
    
    public void setVaccinationDate(LocalDate vaccinationDate) {
        this.vaccinationDate = vaccinationDate;
    }
    
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }
    
    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
        calculateDaysUntilDue();
        calculateStatus();
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
    
    public boolean isReminderEnabled() {
        return reminderEnabled;
    }
    
    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        calculateStatus();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public long getDaysUntilDue() {
        return daysUntilDue;
    }
    
    public void setDaysUntilDue(long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
