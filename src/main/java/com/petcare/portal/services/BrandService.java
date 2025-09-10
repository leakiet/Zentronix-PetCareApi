package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.brandDto.BrandResponse;
import com.petcare.portal.entities.Brand;

public interface BrandService {
  List<BrandResponse> getAllBrands();
  BrandResponse createBrand(Brand brand);
  BrandResponse getBrandById(Long id);
  BrandResponse updateBrand(Long id, Brand brand);
  void deleteBrand(Long id);
}
