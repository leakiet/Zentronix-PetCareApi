package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.brandDto.BrandResponse;
import com.petcare.portal.entities.Brand;
import com.petcare.portal.repositories.BrandRepository;
import com.petcare.portal.services.BrandService;

@Service
public class BrandServiceImpl implements BrandService {

  @Autowired
  private BrandRepository brandRepository;

  @Override
  public List<BrandResponse> getAllBrands() {
    try {
      List<Brand> brands = brandRepository.findAll();
      return brands.stream()
          .map(this::convertToResponse)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving brands: " + e.getMessage());
    }
  }

  @Override
  public BrandResponse createBrand(Brand brand) {
    try {
      Brand savedBrand = brandRepository.save(brand);
      return convertToResponse(savedBrand);
    } catch (Exception e) {
      throw new RuntimeException("Error creating brand: " + e.getMessage());
    }
  }

  @Override
  public BrandResponse getBrandById(Long id) {
    try {
      Brand brand = brandRepository.findById(id).orElse(null);
      return brand != null ? convertToResponse(brand) : null;
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving brand by ID: " + e.getMessage());
    }
  }

  @Override
  public BrandResponse updateBrand(Long id, Brand brandDetails) {
    try {
      Brand brand = brandRepository.findById(id).orElse(null);
      if (brand != null) {
        brand.setName(brandDetails.getName());
        Brand updatedBrand = brandRepository.save(brand);
        return convertToResponse(updatedBrand);
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("Error updating brand: " + e.getMessage());
    }
  }

  @Override
  public void deleteBrand(Long id) {
    try {
      brandRepository.deleteById(id);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting brand: " + e.getMessage());
    }
  }

  private BrandResponse convertToResponse(Brand brand) {
    try {
      BrandResponse response = new BrandResponse();
      response.setId(brand.getId());
      response.setName(brand.getName());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error converting brand to response: " + e.getMessage());
    }
  }
}
