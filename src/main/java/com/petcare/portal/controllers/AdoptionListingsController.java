package com.petcare.portal.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
      System.err.println("Error in getAdoptionListingById: " + e.getMessage());
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/shelter/{shelterId}")
  public ResponseEntity<List<AdoptionListingsResponse>> getAllAdoptionByShelterId(
      @PathVariable("shelterId") Long shelterId) {
    try {
      List<AdoptionListingsResponse> responses = adoptionListingsService.getAllAdoptionByShelterId(shelterId);
      return ResponseEntity.ok(responses);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(null);
    }
  }

  @PutMapping("/{listingId}/approve")
  public ResponseEntity<?> approveAdoptionRequest(@PathVariable("listingId") Long listingId,
      @RequestBody Map<String, Long> requestBody) {
    try {
      Long requestId = requestBody.get("requestId");
      Long ownerId = requestBody.get("ownerId");
      if (requestId == null || ownerId == null) {
        return ResponseEntity.badRequest().body("requestId and ownerId are required");
      }
      AdoptionListingsResponse response = adoptionListingsService.approveAdoptionRequest(listingId, requestId, ownerId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
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
      request.setPetName(existing.getPetName());
      request.setDescription(existing.getDescription());
      request.setAge(existing.getAge());
      request.setGender(existing.getGender());
      request.setBreedId(existing.getBreed().getId());
      request.setSpecies(existing.getSpecies());
      request.setStatus(existing.getStatus());
      request.setAdoptionStatus(existing.getAdoptionStatus());
      request.setShelterId(existing.getShelter().getId().toString());
      request.setImage(imageUrl);

      AdoptionListingsResponse response = adoptionListingsService.updateAdoptionListing(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("Error updating adoption listing image: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<Page<AdoptionListingsResponse>> getAllAdoptionListings(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortField,
      @RequestParam(defaultValue = "asc") String sortDir,
      @RequestParam(required = false) String species,
      @RequestParam(required = false) Long breedId,
      @RequestParam(required = false) String gender,
      @RequestParam(required = false) Integer minAge,
      @RequestParam(required = false) Integer maxAge) {
    try {
      Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
      Pageable pageable = PageRequest.of(page, size, sort);
      Page<AdoptionListingsResponse> responses = adoptionListingsService.getAllAdoptionListings(pageable, species,
          breedId, gender, minAge, maxAge);
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
