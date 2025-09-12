package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.PetDto.PetRequestDto;
import com.petcare.portal.dtos.PetDto.PetResponseDto;

public interface PetService {
    List<PetResponseDto> getMyPets(Long ownerId);
    PetResponseDto createPet(Long ownerId, PetRequestDto requestDto);
}
