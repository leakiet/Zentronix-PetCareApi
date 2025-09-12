package com.petcare.portal.services;

import com.petcare.portal.dtos.petVaccinationDto.CreatePetVaccinationRequest;
import com.petcare.portal.dtos.petVaccinationDto.PetVaccinationResponse;
import com.petcare.portal.dtos.petVaccinationDto.UpdatePetVaccinationRequest;

import java.util.List;

public interface PetVaccinationService {
    
    PetVaccinationResponse createVaccination(CreatePetVaccinationRequest request);
    
    PetVaccinationResponse updateVaccination(Long id, UpdatePetVaccinationRequest request);
    
    void deleteVaccination(Long id);
    
    List<PetVaccinationResponse> getVaccinationsByPetId(Long petId);
}
