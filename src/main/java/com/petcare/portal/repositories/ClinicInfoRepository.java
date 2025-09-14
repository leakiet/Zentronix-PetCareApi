package com.petcare.portal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.ClinicInfo;
import com.petcare.portal.enums.Specialization;

public interface ClinicInfoRepository extends JpaRepository<ClinicInfo, Long> {
  Optional<ClinicInfo> findByVeterinarians_Id(Long veterinarianId);

  List<ClinicInfo> findBySpecialization(Specialization specialization);

  List<ClinicInfo> findByAddressContaining(String address);

  List<ClinicInfo> findBySpecializationAndAddressContaining(Specialization specialization, String address);
}
