package com.petcare.portal.dtos.petVaccinationDto;

import com.petcare.portal.enums.VaccineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreatePetVaccinationRequest {
    
    @NotNull(message = "Pet ID is required")
    private Long petId;
    
    private Long vetId; // nullable
    
    @NotNull(message = "Vaccine type is required")
    private VaccineType vaccineType;
    
    @NotBlank(message = "Vaccine name is required")
    private String vaccineName;
    
    @NotNull(message = "Vaccination date is required")
    private LocalDate vaccinationDate;
    
    @NotNull(message = "Next due date is required")
    private LocalDate nextDueDate;
    
    private String notes;
    private String vetName;
    private boolean reminderEnabled = true;
    private boolean isCompleted = false;
    
    // Constructors
    public CreatePetVaccinationRequest() {}
    
    public CreatePetVaccinationRequest(Long petId, Long vetId, VaccineType vaccineType, 
                                     String vaccineName, LocalDate vaccinationDate, 
                                     LocalDate nextDueDate, String notes, String vetName,
                                     boolean reminderEnabled, boolean isCompleted) {
        this.petId = petId;
        this.vetId = vetId;
        this.vaccineType = vaccineType;
        this.vaccineName = vaccineName;
        this.vaccinationDate = vaccinationDate;
        this.nextDueDate = nextDueDate;
        this.notes = notes;
        this.vetName = vetName;
        this.reminderEnabled = reminderEnabled;
        this.isCompleted = isCompleted;
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
}
