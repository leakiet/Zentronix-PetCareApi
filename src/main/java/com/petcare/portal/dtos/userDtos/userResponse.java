package com.petcare.portal.dtos.userDtos;

import com.petcare.portal.dtos.authDtos.AddressRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class userResponse {
  private String firstName;
  private String lastName;
  private String companyName;
  private String phone;
  private String email;
  private String role;
  private Boolean isActive;
  private AddressRequest address;
  private String message;
}
