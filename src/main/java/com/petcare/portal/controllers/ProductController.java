package com.petcare.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.dtos.brandDto.BrandResponse;
import com.petcare.portal.dtos.categoryDto.CategoryResponse;
import com.petcare.portal.dtos.productDto.ProductRequest;
import com.petcare.portal.dtos.productDto.ProductResponse;
import com.petcare.portal.entities.Brand;
import com.petcare.portal.entities.Category;
import com.petcare.portal.services.BrandService;
import com.petcare.portal.services.CategoryService;
import com.petcare.portal.services.ProductService;
import com.petcare.portal.utils.ImageUtils;
import com.petcare.portal.utils.SlugUtils;

@RestController
@RequestMapping("/apis/v1/products")
public class ProductController {

  @Autowired
  ProductService productService;

  @Autowired
  CategoryService categoryService;

  @Autowired
  BrandService brandService;

  @Autowired
  ImageUtils imageUtils;

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
    try {
      ProductResponse product = productService.getProductById(id);
      if (product != null) {
        return ResponseEntity.ok(product);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("")
  public ResponseEntity<Page<ProductResponse>> getAllProducts(
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) List<Long> categoryIds,
      @RequestParam(required = false) List<Long> brandIds,
      @RequestParam(required = false) Double minPrice,
      @RequestParam(required = false) Double maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {
    try {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
      Page<ProductResponse> products = productService.getAllProductsWithFilters(searchTerm, categoryIds, brandIds,
          minPrice, maxPrice, pageable);
      return ResponseEntity.ok(products);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/slug/{slug}")
  public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable("slug") String slug) {
    try {
      ProductResponse product = productService.getProductBySlug(slug);
      if (product != null) {
        return ResponseEntity.ok(product);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping("")
  public ResponseEntity<?> createProduct(@ModelAttribute ProductRequest request,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      String baseSlug = SlugUtils.toSlug(request.getName());
      String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
          slug -> productService.existsBySlug(slug));
      request.setSlug(uniqueSlug);

      if (file != null && !file.isEmpty()) {
        String imageUrl = imageUtils.uploadImage(file);
        request.setImage(imageUrl);
      }

      ProductResponse createdProduct = productService.createProduct(request);
      return ResponseEntity.ok(createdProduct);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/{id}/image")
  public ResponseEntity<?> updateProductImage(
      @PathVariable Long id,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      ProductResponse existingProduct = productService.getProductById(id);
      if (existingProduct == null) {
        return ResponseEntity.status(404).body("Product not found");
      }

      if (existingProduct.getImage() != null && !existingProduct.getImage().isEmpty()) {
        imageUtils.deleteImage(existingProduct.getImage());
      }

      String imageUrl = imageUtils.uploadImage(file);

      ProductRequest request = new ProductRequest();
      request.setImage(imageUrl);

      ProductResponse updatedProduct = productService.updateProduct(id, request);
      return ResponseEntity.ok(updatedProduct);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateProduct(
      @PathVariable("id") Long id,
      @RequestBody ProductRequest request) {
    try {
      ProductResponse existingProduct = productService.getProductById(id);
      if (existingProduct == null) {
        return ResponseEntity.status(404).body("Product not found");
      }

      if (!existingProduct.getName().equals(request.getName())) {
        String baseSlug = SlugUtils.toSlug(request.getName());
        String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
            slug -> !slug.equals(existingProduct.getSlug()) && productService.existsBySlug(slug));
        request.setSlug(uniqueSlug);
      } else {
        request.setSlug(existingProduct.getSlug());
      }

      request.setImage(existingProduct.getImage());

      ProductResponse updatedProduct = productService.updateProduct(id, request);
      return ResponseEntity.ok(updatedProduct);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
    try {
      productService.deleteProduct(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/categories")
  public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    try {
      List<CategoryResponse> categories = categoryService.getAllCategories();
      return ResponseEntity.ok(categories);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/categories/{id}")
  public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Long id) {
    try {
      CategoryResponse category = categoryService.getCategoryById(id);
      if (category != null) {
        return ResponseEntity.ok(category);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping("/categories")
  public ResponseEntity<?> createCategory(@RequestBody Category category) {
    try {
      CategoryResponse createdCategory = categoryService.createCategory(category);
      if (createdCategory != null) {
        return ResponseEntity.ok(createdCategory);
      } else {
        return ResponseEntity.status(400).body("Failed to create category");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/categories/{id}")
  public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody Category category) {
    try {
      CategoryResponse updatedCategory = categoryService.updateCategory(id, category);
      if (updatedCategory != null) {
        return ResponseEntity.ok(updatedCategory);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @DeleteMapping("/categories/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
    try {
      categoryService.deleteCategory(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/brands")
  public ResponseEntity<List<BrandResponse>> getAllBrands() {
    try {
      List<BrandResponse> brands = brandService.getAllBrands();
      return ResponseEntity.ok(brands);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @GetMapping("/brands/{id}")
  public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
    try {
      BrandResponse brand = brandService.getBrandById(id);
      if (brand != null) {
        return ResponseEntity.ok(brand);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body(null);
    }
  }

  @PostMapping("/brands")
  public ResponseEntity<?> createBrand(@RequestBody Brand brand) {
    try {
      BrandResponse createdBrand = brandService.createBrand(brand);
      if (createdBrand != null) {
        return ResponseEntity.ok(createdBrand);
      } else {
        return ResponseEntity.status(400).body("Failed to create brand");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/brands/{id}")
  public ResponseEntity<?> updateBrand(@PathVariable Long id, @RequestBody Brand brand) {
    try {
      BrandResponse updatedBrand = brandService.updateBrand(id, brand);
      if (updatedBrand != null) {
        return ResponseEntity.ok(updatedBrand);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @DeleteMapping("/brands/{id}")
  public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
    try {
      brandService.deleteBrand(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

}
