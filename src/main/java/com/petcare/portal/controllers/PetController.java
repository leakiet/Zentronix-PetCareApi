package com.petcare.portal.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.dtos.PetDto.PetRequestDto;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.PetService;

@RestController
@RequestMapping("/apis/v1/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyPets(
            @RequestParam(value = "ownerId", required = false) Long ownerId, Principal principal) {
        // Check if user is authenticated
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in.");
        }

        // Fetch the authenticated user
        User currentUser = userRepository.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(404).body("User not found.");
        }

        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(400).body("Invalid user ID.");
        }

        // If ownerId is provided, ensure it matches the authenticated user's ID
        if (ownerId != null && !ownerId.equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden: You can only access your own pets.");
        }

        // Use the authenticated user's ID if ownerId is not provided
        return ResponseEntity.ok(petService.getMyPets(userId));
    }

    @PostMapping
    public ResponseEntity<?> createPet(
            @RequestBody PetRequestDto requestDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in.");
        }

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.status(404).body("User not found.");
        }

        Long userId = user.getId();
        if (userId == null) {
            return ResponseEntity.status(400).body("Invalid user ID.");
        }

        return ResponseEntity.ok(petService.createPet(userId, requestDto));
    }
}