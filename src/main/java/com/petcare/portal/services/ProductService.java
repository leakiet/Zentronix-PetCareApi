package com.petcare.portal.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.petcare.portal.dtos.productDto.ProductRequest;
import com.petcare.portal.dtos.productDto.ProductResponse;
import com.petcare.portal.entities.Product;

public interface ProductService {
  ProductResponse createProduct(Product product);
  ProductResponse createProduct(ProductRequest request);
  Page<ProductResponse> getAllProductsWithFilters(String searchTerm, List<Long> categoryIds, List<Long> brandIds, Double minPrice, Double maxPrice, Pageable pageable);
  ProductResponse getProductById(Long id);
  ProductResponse getProductBySlug(String slug);
  ProductResponse updateProduct(Long id, Product product);
  ProductResponse updateProduct(Long id, ProductRequest request);
  void deleteProduct(Long id);
  boolean existsBySlug(String slug);
}
