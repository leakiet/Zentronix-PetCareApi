package com.petcare.portal.services.impl;

import com.petcare.portal.dtos.petVaccinationDto.CreatePetVaccinationRequest;
import com.petcare.portal.dtos.petVaccinationDto.PetVaccinationResponse;
import com.petcare.portal.dtos.petVaccinationDto.UpdatePetVaccinationRequest;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.PetVaccination;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.PetVaccinationRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.PetVaccinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetVaccinationServiceImpl implements PetVaccinationService {
    
    @Autowired
    private PetVaccinationRepository petVaccinationRepository;
    
    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public PetVaccinationResponse createVaccination(CreatePetVaccinationRequest request) {
        Pet pet = petRepository.findById(request.getPetId())
            .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));
        
        User vet = null;
        if (request.getVetId() != null) {
            vet = userRepository.findById(request.getVetId())
                .orElseThrow(() -> new RuntimeException("Vet not found with id: " + request.getVetId()));
        }
        
        PetVaccination vaccination = new PetVaccination();
        vaccination.setPet(pet);
        vaccination.setVet(vet);
        vaccination.setVaccineType(request.getVaccineType());
        vaccination.setVaccineName(request.getVaccineName());
        vaccination.setVaccinationDate(request.getVaccinationDate());
        vaccination.setNextDueDate(request.getNextDueDate());
        vaccination.setNotes(request.getNotes());
        vaccination.setVetName(request.getVetName());
        vaccination.setReminderEnabled(request.isReminderEnabled());
        vaccination.setCompleted(request.isCompleted());
        
        PetVaccination savedVaccination = petVaccinationRepository.save(vaccination);
        return convertToResponse(savedVaccination);
    }
    
    @Override
    public PetVaccinationResponse updateVaccination(Long id, UpdatePetVaccinationRequest request) {
        PetVaccination vaccination = petVaccinationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vaccination not found with id: " + id));
        
        if (vaccination.getIsDeleted()) {
            throw new RuntimeException("Cannot update deleted vaccination");
        }
        
        if (request.getVaccineType() != null) {
            vaccination.setVaccineType(request.getVaccineType());
        }
        if (request.getVaccineName() != null) {
            vaccination.setVaccineName(request.getVaccineName());
        }
        if (request.getVaccinationDate() != null) {
            vaccination.setVaccinationDate(request.getVaccinationDate());
        }
        if (request.getNextDueDate() != null) {
            vaccination.setNextDueDate(request.getNextDueDate());
        }
        if (request.getNotes() != null) {
            vaccination.setNotes(request.getNotes());
        }
        if (request.getVetName() != null) {
            vaccination.setVetName(request.getVetName());
        }
        vaccination.setReminderEnabled(request.isReminderEnabled());
        vaccination.setCompleted(request.isCompleted());
        if (request.getIsDeleted() != null) {
            vaccination.setIsDeleted(request.getIsDeleted());
        }
        
        PetVaccination updatedVaccination = petVaccinationRepository.save(vaccination);
        return convertToResponse(updatedVaccination);
    }
    
    @Override
    public void deleteVaccination(Long id) {
        PetVaccination vaccination = petVaccinationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vaccination not found with id: " + id));
        
        vaccination.setIsDeleted(true);
        petVaccinationRepository.save(vaccination);
    }
    
    @Override
    public List<PetVaccinationResponse> getVaccinationsByPetId(Long petId) {
        List<PetVaccination> vaccinations = petVaccinationRepository.findByPetIdAndNotDeleted(petId);
        return vaccinations.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private PetVaccinationResponse convertToResponse(PetVaccination vaccination) {
        return new PetVaccinationResponse(
            vaccination.getId(),
            vaccination.getPet().getId(),
            vaccination.getPet().getPetName(),
            vaccination.getVet() != null ? vaccination.getVet().getId() : null,
            vaccination.getVaccineType(),
            vaccination.getVaccineName(),
            vaccination.getVaccinationDate(),
            vaccination.getNextDueDate(),
            vaccination.getNotes(),
            vaccination.getVetName(),
            vaccination.isReminderEnabled(),
            vaccination.isCompleted(),
            vaccination.getCreatedAt(),
            vaccination.getUpdatedAt()
        );
    }
}
