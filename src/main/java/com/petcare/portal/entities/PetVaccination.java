package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petcare.portal.enums.VaccineType;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pet_vaccinations")
public class PetVaccination extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    @JsonBackReference
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "vet_id", nullable = true)
    @JsonBackReference
    private User vet;

    @Enumerated(EnumType.STRING)
    private VaccineType vaccineType;

    private String vaccineName;
    private LocalDate vaccinationDate;
    private LocalDate nextDueDate;
    private String notes;
    private String vetName;
    private boolean reminderEnabled = true;
    private boolean isCompleted = false;
}
