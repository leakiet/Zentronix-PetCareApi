package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.SpeciesDto.SpeciesRequest;
import com.petcare.portal.dtos.SpeciesDto.SpeciesResponse;
import com.petcare.portal.entities.Species;
import com.petcare.portal.repositories.SpeciesRepository;
import com.petcare.portal.services.SpeciesService;

@Service
public class SpeciesServiceImpl implements SpeciesService {

  @Autowired
  private SpeciesRepository speciesRepository;

  @Override
  public SpeciesResponse getSpeciesById(Long id) {
    try {
      Species species = speciesRepository.findById(id).orElseThrow(() -> new RuntimeException("Species not found"));
      SpeciesResponse response = new SpeciesResponse();
      response.setId(species.getId());
      response.setName(species.getName());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving species", e);
    }
  }

  @Override
  public SpeciesResponse createSpecies(SpeciesRequest speciesRequest) {
    try {
      Species species = new Species();
      species.setName(speciesRequest.getName());
      Species savedSpecies = speciesRepository.save(species);
      SpeciesResponse response = new SpeciesResponse();
      response.setId(savedSpecies.getId());
      response.setName(savedSpecies.getName());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error creating species", e);
    }
  }

  @Override
  public SpeciesResponse updateSpecies(Long id, SpeciesRequest speciesRequest) {
    try {
      Species species = speciesRepository.findById(id).orElseThrow(() -> new RuntimeException("Species not found"));
      species.setName(speciesRequest.getName());
      Species updatedSpecies = speciesRepository.save(species);
      SpeciesResponse response = new SpeciesResponse();
      response.setId(updatedSpecies.getId());
      response.setName(updatedSpecies.getName());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error updating species", e);
    }
  }

  @Override
  public List<SpeciesResponse> getAllSpecies() {
    try {
      List<Species> speciesList = speciesRepository.findAll();
      return speciesList.stream()
          .map(species -> {
            SpeciesResponse response = new SpeciesResponse();
            response.setId(species.getId());
            response.setName(species.getName());
            return response;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving species", e);
    }
  }

  @Override
  public void deleteSpecies(Long id) {
    try {
      if (!speciesRepository.existsById(id)) {
        throw new RuntimeException("Species not found");
      }
      speciesRepository.deleteById(id);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting species", e);
    }
  }
  
}
