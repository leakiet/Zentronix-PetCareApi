package com.petcare.portal.dtos.authDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private long id;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String phone;
    private String companyName;
    private String street;
    private String ward;
    private String city;
    private String latitude;
    private String longitude;
    private String token;
    private String tokenType;
    private String refreshToken;
}
