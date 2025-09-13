package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.dtos.AdoptionRequestDtos.AdoptionRequestRes;
import com.petcare.portal.dtos.AdoptionRequestDtos.AdoptionRequestResponse;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.services.AdoptionRequestService;

@RestController
@RequestMapping("/apis/v1/adoption-requests")
public class AdoptionRequestController {

    @Autowired
    private AdoptionRequestService adoptionRequestService;

    @PostMapping
    public ResponseEntity<?> createAdoptionRequest(@RequestBody AdoptionRequestRes dto) {
        try {
            AdoptionRequest request = adoptionRequestService.createAdoptionRequest(
                dto.getOwnerId(), 
                dto.getAdoptionListingId(), 
                dto.getShelterId(),
                dto.getMessage(), 
                dto.getDistance()
            );
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdoptionRequestById(@PathVariable("id") Long id) {
        try {
            AdoptionRequestResponse response = adoptionRequestService.getAdoptionRequestById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAdoptionRequestStatus(@PathVariable("id") Long id, @RequestParam String status) {
        try {
            AdoptionRequestResponse response = adoptionRequestService.updateAdoptionRequestStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdoptionRequest(@PathVariable("id") Long id) {
        try {
            adoptionRequestService.deleteAdoptionRequest(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllAdoptionRequests() {
        try {
            List<AdoptionRequest> requests = adoptionRequestService.getAllAdoptionRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/listing/{adoptionListingId}")
    public ResponseEntity<?> getRequestsByAdoptionListingId(@PathVariable("adoptionListingId") Long adoptionListingId) {
        try {
            List<AdoptionRequest> requests = adoptionRequestService.getRequestsByAdoptionListingId(adoptionListingId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<?> getRequestsByShelterId(@PathVariable("shelterId") Long shelterId) {
        try {
            List<AdoptionRequest> requests = adoptionRequestService.getRequestsByShelterId(shelterId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
