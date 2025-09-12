package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.categoryDto.CategoryResponse;
import com.petcare.portal.entities.Category;

public interface CategoryService {
  CategoryResponse createCategory(Category category);
  List<CategoryResponse> getAllCategories();
  CategoryResponse getCategoryById(Long id);
  CategoryResponse updateCategory(Long id, Category category);
  void deleteCategory(Long id);
}
