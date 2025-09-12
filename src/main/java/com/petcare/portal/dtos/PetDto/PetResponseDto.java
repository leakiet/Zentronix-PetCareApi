package com.petcare.portal.dtos.PetDto;

import com.petcare.portal.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetResponseDto {
    private Long id;
    private String petName;
    private String species;
    private String breed;
    private String image;
    private Gender gender;
    private String age;
}
