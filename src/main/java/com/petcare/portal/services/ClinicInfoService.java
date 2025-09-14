package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoRequest;
import com.petcare.portal.dtos.ClinicInfoDtos.ClinicInfoResponse;

public interface ClinicInfoService {
  ClinicInfoResponse getClinicInfoByVetId(Long vetId);
  ClinicInfoResponse createClinicInfo(ClinicInfoRequest clinicInfoRequest);
  ClinicInfoResponse updateClinicInfo(Long id, ClinicInfoRequest clinicInfoRequest);
  void deleteClinicInfo(Long id);
  // List<ClinicInfoResponse> searchClinicInfos(String specialization, String location);
  List<ClinicInfoResponse> listAllClinicInfos();
}
