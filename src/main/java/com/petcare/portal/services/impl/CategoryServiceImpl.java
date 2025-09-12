package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.categoryDto.CategoryResponse;
import com.petcare.portal.entities.Category;
import com.petcare.portal.repositories.CategoryRepository;
import com.petcare.portal.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  @Override
  public CategoryResponse createCategory(Category category) {
    try {
      Category savedCategory = categoryRepository.save(category);
      CategoryResponse response = new CategoryResponse();
      response.setName(savedCategory.getName());
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public List<CategoryResponse> getAllCategories() {
    try {
      List<Category> categories = categoryRepository.findAll();
      return categories.stream()
          .map(cat -> {
            CategoryResponse response = new CategoryResponse();
            response.setId(cat.getId());
            response.setName(cat.getName());
            return response;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public CategoryResponse getCategoryById(Long id) {
    try {
      Category category = categoryRepository.findById(id).orElse(null);
      if (category != null) {
        CategoryResponse response = new CategoryResponse();
        response.setName(category.getName());
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public CategoryResponse updateCategory(Long id, Category category) {
    try {
      Category existing = categoryRepository.findById(id).orElse(null);
      if (existing != null) {
        existing.setName(category.getName());
        Category updated = categoryRepository.save(existing);
        CategoryResponse response = new CategoryResponse();
        response.setName(updated.getName());
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void deleteCategory(Long id) {
    try {
      categoryRepository.deleteById(id);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
