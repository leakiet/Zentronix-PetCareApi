package com.petcare.portal.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.petcare.portal.enums.Gender;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pets")
public class Pet extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference
    private User owner;

    private String petName;
    private String species;
    private String breed;
    private String image;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String age;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<HealthRecord> healthRecords;

}
