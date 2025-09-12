package com.petcare.portal.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends AbstractEntity {
  private String name;

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;
  
  @ManyToOne
  @JoinColumn(name = "brand_id")
  private Brand brand;

  private double price;

  private String description;

  private int stock;

  private String sku;

  private String image;

  private String slug;

  private int weight;

}
