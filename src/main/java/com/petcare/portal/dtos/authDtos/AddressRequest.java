package com.petcare.portal.dtos.authDtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
  @NotBlank(message = "Street address is required")
  private String street;

  @NotBlank(message = "Ward is required")
  private String ward;

  @NotBlank(message = "City is required")
  private String city;

  private String latitude;
  private String longitude;
}
