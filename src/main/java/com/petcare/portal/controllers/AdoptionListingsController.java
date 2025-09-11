package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.dtos.BreedDto.BreedRequest;
import com.petcare.portal.dtos.BreedDto.BreedResponse;
import com.petcare.portal.services.AdoptionListingsService;
import com.petcare.portal.services.BreedService;
import com.petcare.portal.utils.ImageUtils;

@RestController
@RequestMapping("/apis/v1/adoption-listings")
public class AdoptionListingsController {

  @Autowired
  private AdoptionListingsService adoptionListingsService;


  @Autowired
  private BreedService breedService;

  @Autowired
  private ImageUtils imageUtils;

  @GetMapping("/{id}")
  public ResponseEntity<AdoptionListingsResponse> getAdoptionListingById(@PathVariable("id") Long id) {
    try {
      AdoptionListingsResponse response = adoptionListingsService.getAdoptionListingById(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping
  public ResponseEntity<?> createAdoptionListing(@ModelAttribute AdoptionListingsRequest request,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      if (file != null && !file.isEmpty()) {
        String imageUrl = imageUtils.uploadImage(file);
        request.setImage(imageUrl);
      }
      AdoptionListingsResponse response = adoptionListingsService.createAdoptionListing(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateAdoptionListing(@PathVariable("id") Long id,
      @RequestBody AdoptionListingsRequest request) {
    try {
      AdoptionListingsResponse existing = adoptionListingsService.getAdoptionListingById(id);
      request.setImage(existing.getImage());
      AdoptionListingsResponse response = adoptionListingsService.updateAdoptionListing(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/{id}/image")
  public ResponseEntity<?> updateAdoptionListingImage(@PathVariable("id") Long id,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      AdoptionListingsResponse existing = adoptionListingsService.getAdoptionListingById(id);
      if (existing.getImage() != null && !existing.getImage().isEmpty()) {
        imageUtils.deleteImage(existing.getImage());
      }
      String imageUrl = imageUtils.uploadImage(file);
      AdoptionListingsRequest request = new AdoptionListingsRequest();
      request.setImage(imageUrl);
      AdoptionListingsResponse response = adoptionListingsService.updateAdoptionListing(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<List<AdoptionListingsResponse>> getAllAdoptionListings() {
    try {
      List<AdoptionListingsResponse> responses = adoptionListingsService.getAllAdoptionListings();
      return ResponseEntity.ok(responses);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAdoptionListing(@PathVariable("id") Long id) {
    try {
      AdoptionListingsResponse existing = adoptionListingsService.getAdoptionListingById(id);
      if (existing.getImage() != null && !existing.getImage().isEmpty()) {
        imageUtils.deleteImage(existing.getImage());
      }
      adoptionListingsService.deleteAdoptionListing(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  // @GetMapping("/species/{id}")
  // public ResponseEntity<SpeciesResponse> getSpeciesById(@PathVariable("id") Long id) {
  //   try {
  //     SpeciesResponse response = speciesService.getSpeciesById(id);
  //     return ResponseEntity.ok(response);
  //   } catch (Exception e) {
  //     return ResponseEntity.status(500).body(null);
  //   }
  // }

  // @PostMapping("/species")
  // public ResponseEntity<SpeciesResponse> createSpecies(@RequestBody SpeciesRequest request) {
  //   try {
  //     SpeciesResponse response = speciesService.createSpecies(request);
  //     return ResponseEntity.ok(response);
  //   } catch (Exception e) {
  //     return ResponseEntity.status(500).body(null);
  //   }
  // }

  // @PutMapping("/species/{id}")
  // public ResponseEntity<SpeciesResponse> updateSpecies(@PathVariable("id") Long id, @RequestBody SpeciesRequest request) {
  //   try {
  //     SpeciesResponse response = speciesService.updateSpecies(id, request);
  //     return ResponseEntity.ok(response);
  //   } catch (Exception e) {
  //     return ResponseEntity.status(500).body(null);
  //   }
  // }

  // @GetMapping("/species")
  // public ResponseEntity<List<SpeciesResponse>> getAllSpecies() {
  //   try {
  //     List<SpeciesResponse> responses = speciesService.getAllSpecies();
  //     return ResponseEntity.ok(responses);
  //   } catch (Exception e) {
  //     return ResponseEntity.status(500).body(null);
  //   }
  // }

  // @DeleteMapping("/species/{id}")
  // public ResponseEntity<Void> deleteSpecies(@PathVariable("id") Long id) {
  //   try {
  //     speciesService.deleteSpecies(id);
  //     return ResponseEntity.noContent().build();
  //   } catch (Exception e) {
  //     return ResponseEntity.status(500).build();
  //   }
  // }

  @GetMapping("/breeds/{id}")
  public ResponseEntity<BreedResponse> getBreedById(@PathVariable("id") Long id) {
    try {
      BreedResponse response = breedService.getBreedById(id);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping("/breeds")
  public ResponseEntity<BreedResponse> createBreed(@RequestBody BreedRequest request) {
    try {
      BreedResponse response = breedService.createBreed(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @PutMapping("/breeds/{id}")
  public ResponseEntity<BreedResponse> updateBreed(@PathVariable("id") Long id, @RequestBody BreedRequest request) {
    try {
      BreedResponse response = breedService.updateBreed(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/breeds")
  public ResponseEntity<List<BreedResponse>> getAllBreeds() {
    try {
      List<BreedResponse> responses = breedService.getAllBreeds();
      return ResponseEntity.ok(responses);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @DeleteMapping("/breeds/{id}")
  public ResponseEntity<Void> deleteBreed(@PathVariable("id") Long id) {
    try {
      breedService.deleteBreed(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }
}
