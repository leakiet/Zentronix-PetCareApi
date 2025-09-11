package com.petcare.portal.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "breeds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Breed extends AbstractEntity {
  private String name;
}
