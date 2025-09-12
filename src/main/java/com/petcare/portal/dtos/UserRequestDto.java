package com.petcare.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    private String firstName;
    private String lastName;
    private String companyName;
    private String password;          
    private String phone;
    private String email;
    private String gender;           
    private String role;             
    private Boolean isActive;
    private String oauthProvider;    
    private String oauthProviderId;   
    private Boolean isOauthUser;
    private String street;
    private String ward;
    private String city;
    private Double latitude;
    private Double longitude;
}
