package com.petcare.portal.dtos.productDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
  private Long id;
  private String name;
  private String category;
  private double price;
  private String description;
  private int stock;
  private String sku;
  private String image;
  private int weight;
  private String brand;
  private String slug;
}
