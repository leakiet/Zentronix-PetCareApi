package com.petcare.portal.dtos.productDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
  private Long id;
  private String name;
  private Long categoryId;
  private double price;
  private String description;
  private int stock;
  private String sku;
  private String image;
  private int weight;
  private Long brandId;
  private String slug;
}
