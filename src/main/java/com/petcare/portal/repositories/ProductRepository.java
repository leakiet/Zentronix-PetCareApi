package com.petcare.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petcare.portal.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
        SELECT p FROM Product p
        WHERE (:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
          AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)
          AND (:brandIds IS NULL OR p.brand.id IN :brandIds)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<Product> findProductsWithFilters(
        @Param("searchTerm") String searchTerm,
        @Param("categoryIds") List<Long> categoryIds,
        @Param("brandIds") List<Long> brandIds,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        Pageable pageable
    );
}
