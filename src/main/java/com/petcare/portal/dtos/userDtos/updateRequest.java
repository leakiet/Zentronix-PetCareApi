package com.petcare.portal.dtos.userDtos;

import com.petcare.portal.dtos.authDtos.AddressRequest;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class updateRequest {
  private String firstName;
  private String lastName;
  private String companyName;
  private String phone;
  @Email(message = "Invalid email format")
  private String email;
  private String password; // Optional for password updates
  private String role;
  private AddressRequest address;
}
