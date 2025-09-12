package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.entities.Pet;
import com.petcare.portal.services.PetService;
import com.petcare.portal.dtos.petDto.createRequest;
import com.petcare.portal.dtos.petDto.updateRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.validation.Valid;




@RestController
@RequestMapping("/apis/v1/pets")
public class PetController {
  @Autowired
  private PetService petService;

  @PostMapping("/create")
  public ResponseEntity<Pet> addPet(@Valid @RequestBody createRequest request) {
    try {
      Pet savedPet = petService.createPet(request);
      return ResponseEntity.ok(savedPet);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/customer/{userId}")
  public ResponseEntity<List<Pet>> getPetsByCustomerId(@PathVariable("userId") Long userId) {
    try {
      List<Pet> pets = petService.getPetsByCustomerId(userId);
      return ResponseEntity.ok(pets);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<Pet> getPetById(@PathVariable("id") Long id) {
    try {
      Pet pet = petService.getPetById(id);
      return ResponseEntity.ok(pet);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/update/{petId}")
  public ResponseEntity<Pet> updatePet(@PathVariable("petId") Long petId, @Valid @RequestBody updateRequest request) {
    try {
      Pet updatedPet = petService.updatePet(petId, request);
      return ResponseEntity.ok(updatedPet);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
  
}
