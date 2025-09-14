package com.petcare.portal.controllers;

import java.util.List;
import java.util.stream.Collectors;

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

import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoRequest;
import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoResponse;
import com.petcare.portal.entities.User;
import com.petcare.portal.services.ClinicInfoService;

@RestController
@RequestMapping("/apis/v1/clinic-info")
public class ClinicInfoController {

  @Autowired
  private ClinicInfoService clinicInfoService;

  @GetMapping("/vet/{vetId}")
  public ResponseEntity<ClinicInfoResponse> getClinicInfoByVetId(@PathVariable("vetId") Long vetId) {
    try {
      ClinicInfoResponse response = clinicInfoService.getClinicInfoByVetId(vetId);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  public ResponseEntity<ClinicInfoResponse> createClinicInfo(@RequestBody ClinicInfoRequest clinicInfoRequest) {
    try {
      ClinicInfoResponse response = clinicInfoService.createClinicInfo(clinicInfoRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClinicInfoResponse> updateClinicInfo(
      @PathVariable("id") Long id,
      @RequestBody ClinicInfoRequest clinicInfoRequest) {
    try {
      ClinicInfoResponse response = clinicInfoService.updateClinicInfo(id, clinicInfoRequest);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteClinicInfo(@PathVariable("id") Long id) {
    try {
      clinicInfoService.deleteClinicInfo(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  // @GetMapping("/search")
  // public ResponseEntity<List<ClinicInfoResponse>> searchClinicInfos(
  // @RequestParam(required = false) String specialization,
  // @RequestParam(required = false) String location) {
  // try {
  // List<ClinicInfoResponse> responses =
  // clinicInfoService.searchClinicInfos(specialization, location);
  // return ResponseEntity.ok(responses);
  // } catch (RuntimeException e) {
  // return ResponseEntity.badRequest().build();
  // }
  // }

  // @GetMapping("/vets")
  // public ResponseEntity<List<User>> getAvailableVets(
  // @RequestParam(required = false) String petCondition,
  // @RequestParam(required = false) String location,
  // @RequestParam(required = false) Double latitude,
  // @RequestParam(required = false) Double longitude) {
  // try {
  // // Map petCondition to specialization
  // String specialization = null;
  // if (petCondition != null && !petCondition.isEmpty()) {
  // specialization = mapPetConditionToSpecialization(petCondition);
  // }

  // List<ClinicInfoResponse> clinicInfos =
  // clinicInfoService.searchClinicInfos(specialization, location);

  // List<User> vets = clinicInfos.stream()
  // .filter(clinic -> clinic.getVeterinarians() != null)
  // .map(ClinicInfoResponse::getVeterinarians)
  // .collect(Collectors.toList());

  // return ResponseEntity.ok(vets);
  // } catch (RuntimeException e) {
  // return ResponseEntity.badRequest().build();
  // }
  // }

  // private String mapPetConditionToSpecialization(String petCondition) {
  // switch (petCondition.toLowerCase()) {
  // case "general checkup": return "GENERAL_CHECKUP";
  // case "vaccination": return "VACCINATION";
  // case "dental care": return "DENTAL_CARE";
  // case "skin issues": return "SKIN_ISSUES";
  // case "digestive problems": return "DIGESTIVE_PROBLEMS";
  // case "injury/trauma": return "INJURY_TRAUMA";
  // case "behavioral issues": return "BEHAVIORAL_ISSUES";
  // case "senior pet care": return "SENIOR_PET_CARE";
  // case "emergency care": return "EMERGENCY_CARE";
  // default: return "OTHERS";
  // }
  // }

  @GetMapping("/all")
  public ResponseEntity<List<ClinicInfoResponse>> listAllClinicInfos() {
    try {
      List<ClinicInfoResponse> responses = clinicInfoService.listAllClinicInfos();
      return ResponseEntity.ok(responses);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
