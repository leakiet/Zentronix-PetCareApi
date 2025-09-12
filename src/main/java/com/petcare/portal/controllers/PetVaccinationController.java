package com.petcare.portal.controllers;

import com.petcare.portal.dtos.petVaccinationDto.CreatePetVaccinationRequest;
import com.petcare.portal.dtos.petVaccinationDto.PetVaccinationResponse;
import com.petcare.portal.dtos.petVaccinationDto.UpdatePetVaccinationRequest;
import com.petcare.portal.services.PetVaccinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/apis/v1/pet-vaccinations")
public class PetVaccinationController {
    
    @Autowired
    private PetVaccinationService petVaccinationService;
    
    @PostMapping
    public ResponseEntity<PetVaccinationResponse> createVaccination(@Valid @RequestBody CreatePetVaccinationRequest request) {
        try {
            PetVaccinationResponse response = petVaccinationService.createVaccination(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PetVaccinationResponse> updateVaccination(@PathVariable("id") Long id, @Valid @RequestBody UpdatePetVaccinationRequest request) {
        try {
            PetVaccinationResponse response = petVaccinationService.updateVaccination(id, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<String> deleteVaccination(@PathVariable("id") Long id) {
        try {
            petVaccinationService.deleteVaccination(id);
            return ResponseEntity.ok("Vaccination deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<PetVaccinationResponse>> getVaccinationsByPetId(@PathVariable("petId") Long petId) {
        try {
            List<PetVaccinationResponse> responses = petVaccinationService.getVaccinationsByPetId(petId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
