package com.petcare.portal.dtos.petVaccinationDto;

import com.petcare.portal.enums.VaccineType;
import java.time.LocalDate;

public class UpdatePetVaccinationRequest {
    
    private VaccineType vaccineType;
    private String vaccineName;
    private LocalDate vaccinationDate;
    private LocalDate nextDueDate;
    private String notes;
    private String vetName;
    private boolean reminderEnabled;
    private boolean isCompleted;
    private Boolean isDeleted;
    
    // Constructors
    public UpdatePetVaccinationRequest() {}
    
    public UpdatePetVaccinationRequest(VaccineType vaccineType, String vaccineName, 
                                     LocalDate vaccinationDate, LocalDate nextDueDate, 
                                     String notes, String vetName, boolean reminderEnabled,
                                     boolean isCompleted, Boolean isDeleted) {
        this.vaccineType = vaccineType;
        this.vaccineName = vaccineName;
        this.vaccinationDate = vaccinationDate;
        this.nextDueDate = nextDueDate;
        this.notes = notes;
        this.vetName = vetName;
        this.reminderEnabled = reminderEnabled;
        this.isCompleted = isCompleted;
        this.isDeleted = isDeleted;
    }
    
    // Getters and Setters
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
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
