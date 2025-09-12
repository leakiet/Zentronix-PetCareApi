package com.petcare.portal.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.productDto.ProductRequest;
import com.petcare.portal.dtos.productDto.ProductResponse;
import com.petcare.portal.entities.Brand;
import com.petcare.portal.entities.Category;
import com.petcare.portal.entities.Product;
import com.petcare.portal.repositories.BrandRepository;
import com.petcare.portal.repositories.CategoryRepository;
import com.petcare.portal.repositories.ProductRepository;
import com.petcare.portal.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private BrandRepository brandRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public ProductResponse createProduct(Product product) {
    try {
      Product savedProduct = productRepository.save(product);
      ProductResponse response = modelMapper.map(savedProduct, ProductResponse.class);
      if (savedProduct.getCategory() != null) {
        response.setCategory(savedProduct.getCategory().getName());
      }
      if (savedProduct.getBrand() != null) {
        response.setBrand(savedProduct.getBrand().getName());
      }
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ProductResponse createProduct(ProductRequest request) {
    try {
      Product product = new Product();
      modelMapper.map(request, product);

      if (request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        if (category == null) {
          throw new IllegalArgumentException("Invalid category ID: Category does not exist");
        }
        product.setCategory(category);
      } else {
        throw new IllegalArgumentException("Category ID is required");
      }

      if (request.getBrandId() != null) {
        Brand brand = brandRepository.findById(request.getBrandId()).orElse(null);
        if (brand != null) {
          product.setBrand(brand);
        }
      }

      Product savedProduct = productRepository.save(product);
      ProductResponse response = modelMapper.map(savedProduct, ProductResponse.class);
      if (savedProduct.getCategory() != null) {
        response.setCategory(savedProduct.getCategory().getName());
      }
      if (savedProduct.getBrand() != null) {
        response.setBrand(savedProduct.getBrand().getName());
      }
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Page<ProductResponse> getAllProductsWithFilters(String searchTerm, List<Long> categoryIds, List<Long> brandIds,
      Double minPrice, Double maxPrice, Pageable pageable) {
    try {
      Page<Product> products = productRepository.findProductsWithFilters(searchTerm, categoryIds, brandIds, minPrice,
          maxPrice, pageable);
      return products.map(prod -> {
        ProductResponse response = modelMapper.map(prod, ProductResponse.class);
        if (prod.getCategory() != null) {
          response.setCategory(prod.getCategory().getName());
        }
        if (prod.getBrand() != null) {
          response.setBrand(prod.getBrand().getName());
        }
        return response;
      });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ProductResponse getProductById(Long id) {
    try {
      Product product = productRepository.findById(id).orElse(null);
      if (product != null) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        if (product.getCategory() != null) {
          response.setCategory(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
          response.setBrand(product.getBrand().getName());
        }
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ProductResponse getProductBySlug(String slug) {
    try {
      Product product = productRepository.findBySlug(slug);
      if (product != null) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        if (product.getCategory() != null) {
          response.setCategory(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
          response.setBrand(product.getBrand().getName());
        }
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ProductResponse updateProduct(Long id, Product product) {
    try {
      Product existing = productRepository.findById(id).orElse(null);
      if (existing != null) {
        modelMapper.map(product, existing);
        Product updated = productRepository.save(existing);
        ProductResponse response = modelMapper.map(updated, ProductResponse.class);
        if (updated.getCategory() != null) {
          response.setCategory(updated.getCategory().getName());
        }
        if (updated.getBrand() != null) {
          response.setBrand(updated.getBrand().getName());
        }
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ProductResponse updateProduct(Long id, ProductRequest request) {
    try {
      Product existing = productRepository.findById(id).orElse(null);
      if (existing != null) {
        modelMapper.map(request, existing);
        Product updated = productRepository.save(existing);
        ProductResponse response = modelMapper.map(updated, ProductResponse.class);
        if (updated.getCategory() != null) {
          response.setCategory(updated.getCategory().getName());
        }
        if (updated.getBrand() != null) {
          response.setBrand(updated.getBrand().getName());
        }
        return response;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void deleteProduct(Long id) {
    try {
      productRepository.deleteById(id);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean existsBySlug(String slug) {
    try {
      return productRepository.existsBySlug(slug);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
