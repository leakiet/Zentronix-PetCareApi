package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.PetDto.PetRequestDto;
import com.petcare.portal.dtos.PetDto.PetResponseDto;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.PetService;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PetResponseDto> getMyPets(Long ownerId) {
        return petRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PetResponseDto createPet(Long ownerId, PetRequestDto requestDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pet pet = new Pet();
        pet.setOwner(owner);
        pet.setPetName(requestDto.getPetName());
        pet.setSpecies(requestDto.getSpecies());
        pet.setBreed(requestDto.getBreed());
        pet.setImage(requestDto.getImage());
        pet.setGender(requestDto.getGender());
        pet.setAge(requestDto.getAge());

        Pet savedPet = petRepository.save(pet);
        return mapToResponseDto(savedPet);
    }


    private PetResponseDto mapToResponseDto(Pet pet) {
        return PetResponseDto.builder()
                .id(pet.getId())
                .petName(pet.getPetName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .image(pet.getImage())
                .gender(pet.getGender())
                .age(pet.getAge())
                .build();
    }
}
