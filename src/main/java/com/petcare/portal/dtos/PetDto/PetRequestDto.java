package com.petcare.portal.dtos.PetDto;

import com.petcare.portal.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetRequestDto {
    private String petName;
    private String species;
    private String breed;
    private String image;
    private Gender gender;
    private String age;
}
