package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.BreedDto.BreedRequest;
import com.petcare.portal.dtos.BreedDto.BreedResponse;

public interface BreedService {
  BreedResponse getBreedById(Long id);
  BreedResponse createBreed(BreedRequest breedRequest);
  BreedResponse updateBreed(Long id, BreedRequest breedRequest);
  List<BreedResponse> getAllBreeds();
  void deleteBreed(Long id);
}
