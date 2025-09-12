package com.petcare.portal.dtos.petDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    private Boolean isDeleted;
    
}
