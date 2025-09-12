package com.petcare.portal.dtos.petDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class createRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Pet name is required")
    private String petName;
    
    @NotBlank(message = "Species is required")
    private String species;
    
    @NotNull(message = "Breed ID is required")
    private Long breedId;
    
    @Positive(message = "Age must be positive")
    private Integer age;
    
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    private String color;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
}
