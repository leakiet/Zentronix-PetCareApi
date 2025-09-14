package com.petcare.portal.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoRequest;
import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoResponse;
import com.petcare.portal.entities.ClinicInfo;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.Specialization;
import com.petcare.portal.repositories.ClinicInfoRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.ClinicInfoService;

@Service
@Transactional
public class ClinicInfoServiceImpl implements ClinicInfoService {

  @Autowired
  private ClinicInfoRepository clinicInfoRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public ClinicInfoResponse getClinicInfoByVetId(Long vetId) {
    try {
      Optional<ClinicInfo> clinicInfoOpt = clinicInfoRepository.findByVeterinarians_Id(vetId);
      if (clinicInfoOpt.isEmpty()) {
        throw new RuntimeException("Clinic info not found for vet ID: " + vetId);
      }
      return mapToResponse(clinicInfoOpt.get());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving clinic info: " + e.getMessage());
    }
  }

  @Override
  public ClinicInfoResponse createClinicInfo(ClinicInfoRequest clinicInfoRequest) {
    try {
      Optional<User> vetOpt = userRepository.findById(clinicInfoRequest.getVeterinarianId());
      if (vetOpt.isEmpty()) {
        throw new RuntimeException("Vet not found with ID: " + clinicInfoRequest.getVeterinarianId());
      }

      Optional<ClinicInfo> existingClinicInfo = clinicInfoRepository
          .findByVeterinarians_Id(clinicInfoRequest.getVeterinarianId());
      if (existingClinicInfo.isPresent()) {
        throw new RuntimeException("Clinic info already exists for vet ID: " + clinicInfoRequest.getVeterinarianId());
      }

      ClinicInfo clinicInfo = new ClinicInfo();
      clinicInfo.setClinicName(clinicInfoRequest.getClinicName());
      clinicInfo.setAddress(clinicInfoRequest.getAddress());
      clinicInfo.setYearOfExp(clinicInfoRequest.getYearOfExp());
      clinicInfo.setSpecialization(Specialization.valueOf(clinicInfoRequest.getSpecialization()));
      clinicInfo.setOpeningHours(clinicInfoRequest.getOpeningHours());
      clinicInfo.setServicesOffered(clinicInfoRequest.getServicesOffered());
      clinicInfo.setVeterinarians(vetOpt.get());

      ClinicInfo savedClinicInfo = clinicInfoRepository.save(clinicInfo);
      return mapToResponse(savedClinicInfo);
    } catch (Exception e) {
      throw new RuntimeException("Error creating clinic info: " + e.getMessage());
    }
  }

  @Override
  public ClinicInfoResponse updateClinicInfo(Long id, ClinicInfoRequest clinicInfoRequest) {
    try {
      Optional<ClinicInfo> clinicInfoOpt = clinicInfoRepository.findById(id);
      if (clinicInfoOpt.isEmpty()) {
        throw new RuntimeException("Clinic info not found with ID: " + id);
      }

      ClinicInfo existingClinicInfo = clinicInfoOpt.get();

      if (clinicInfoRequest.getClinicName() != null) {
        existingClinicInfo.setClinicName(clinicInfoRequest.getClinicName());
      }
      if (clinicInfoRequest.getAddress() != null) {
        existingClinicInfo.setAddress(clinicInfoRequest.getAddress());
      }
      if (clinicInfoRequest.getYearOfExp() != null) {
        existingClinicInfo.setYearOfExp(clinicInfoRequest.getYearOfExp());
      }
      if (clinicInfoRequest.getSpecialization() != null) {
        existingClinicInfo.setSpecialization(Specialization.valueOf(clinicInfoRequest.getSpecialization()));
      }
      if (clinicInfoRequest.getOpeningHours() != null) {
        existingClinicInfo.setOpeningHours(clinicInfoRequest.getOpeningHours());
      }
      if (clinicInfoRequest.getServicesOffered() != null) {
        existingClinicInfo.setServicesOffered(clinicInfoRequest.getServicesOffered());
      }

      ClinicInfo updatedClinicInfo = clinicInfoRepository.save(existingClinicInfo);
      return mapToResponse(updatedClinicInfo);
    } catch (Exception e) {
      throw new RuntimeException("Error updating clinic info: " + e.getMessage());
    }
  }

  @Override
  public void deleteClinicInfo(Long id) {
    try {
      Optional<ClinicInfo> clinicInfoOpt = clinicInfoRepository.findById(id);
      if (clinicInfoOpt.isEmpty()) {
        throw new RuntimeException("Clinic info not found with ID: " + id);
      }
      clinicInfoRepository.deleteById(id);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting clinic info: " + e.getMessage());
    }
  }

  // @Override
  // public List<ClinicInfoResponse> searchClinicInfos(String specialization,
  // String location) {
  // List<ClinicInfo> clinics = new ArrayList<>();
  // try {
  // if (specialization != null && !specialization.isEmpty()) {
  // Specialization spec = Specialization.valueOf(specialization.toUpperCase());
  // clinics = clinicInfoRepository.findBySpecialization(spec);
  // System.out.println("Clinics by specialization '" + specialization + "': " +
  // clinics.size());
  // clinics.forEach(c -> System.out.println("Clinic: " + c.getClinicName() + ",
  // Spec: " + c.getSpecialization() + ", Address: '" + c.getAddress() + "'"));
  // }

  // if (location != null && !location.isEmpty()) {
  // System.out.println("Filtering by location: '" + location + "'");
  // List<ClinicInfo> originalClinics = new ArrayList<>(clinics); // Sao lưu
  // clinics gốc
  // clinics = clinics.stream()
  // .filter(clinic -> clinic.getAddress() != null &&
  // clinic.getAddress().toLowerCase().contains(location.toLowerCase()))
  // .collect(Collectors.toList());
  // System.out.println("Filtered clinics by location: " + clinics.size());

  // // Nếu không tìm thấy, fallback về clinics gốc (lân cận hoặc tất cả)
  // if (clinics.isEmpty()) {
  // clinics = originalClinics;
  // System.out.println("No clinics found at exact location, returning all clinics
  // for specialization: " + clinics.size());
  // }
  // }

  // return clinics.stream()
  // .map(clinic -> modelMapper.map(clinic, ClinicInfoResponse.class))
  // .collect(Collectors.toList());
  // } catch (Exception e) {
  // System.err.println("Error in searchClinicInfos: " + e.getMessage());
  // return new ArrayList<>();
  // }
  // }

  private ClinicInfoResponse mapToResponse(ClinicInfo clinicInfo) {
    try {
      ClinicInfoResponse response = modelMapper.map(clinicInfo, ClinicInfoResponse.class);
      response.setVeterinarians(clinicInfo.getVeterinarians());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error mapping clinic info to response: " + e.getMessage());
    }
  }

  @Override
  public List<ClinicInfoResponse> listAllClinicInfos() {
    try {
      List<ClinicInfo> allClinics = clinicInfoRepository.findAll();
      return allClinics.stream()
          .map(this::mapToResponse)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving all clinic infos: " + e.getMessage());
    }
  }
}
