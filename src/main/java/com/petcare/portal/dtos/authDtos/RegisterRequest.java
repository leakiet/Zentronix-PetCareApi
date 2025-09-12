package com.petcare.portal.dtos.authDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  private String companyName; // Optional for VET and SHELTER roles

  @NotBlank(message = "Phone number is required")
  private String phone;

  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Role is required")
  private String role;

  @NotNull(message = "Address is required")
  private AddressRequest address;
}
