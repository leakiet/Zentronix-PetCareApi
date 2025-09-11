package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.SpeciesDto.SpeciesRequest;
import com.petcare.portal.dtos.SpeciesDto.SpeciesResponse;

public interface SpeciesService {
  SpeciesResponse getSpeciesById(Long id);
  SpeciesResponse createSpecies(SpeciesRequest speciesRequest);
  SpeciesResponse updateSpecies(Long id, SpeciesRequest speciesRequest);
  List<SpeciesResponse> getAllSpecies();
  void deleteSpecies(Long id);
}
