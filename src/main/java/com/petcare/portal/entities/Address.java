package com.petcare.portal.entities;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable{
  private static final long serialVersionUID = 1L;
  private String street;
  private String ward;
  private String district;
  private String city;
  private String latitude;
  private String longitude;
}
