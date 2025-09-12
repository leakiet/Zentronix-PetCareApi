package com.petcare.portal.dtos.petDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class updateRequest {
    
    @NotBlank(message = "Pet name is required")
    private String petName;
    
    private String species;
    
    private Long breedId;
    
    @Positive(message = "Age must be positive")
    private Integer age;
    
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    private String color;
    
    private String gender;
    
    // Constructors
    public updateRequest() {}
    
    public updateRequest(String petName, String species, Long breedId, 
                        Integer age, Double weight, String color, String gender) {
        this.petName = petName;
        this.species = species;
        this.breedId = breedId;
        this.age = age;
        this.weight = weight;
        this.color = color;
        this.gender = gender;
    }
    
    // Getters and Setters
    public String getPetName() {
        return petName;
    }
    
    public void setPetName(String petName) {
        this.petName = petName;
    }
    
    public String getSpecies() {
        return species;
    }
    
    public void setSpecies(String species) {
        this.species = species;
    }
    
    public Long getBreedId() {
        return breedId;
    }
    
    public void setBreedId(Long breedId) {
        this.breedId = breedId;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
}
