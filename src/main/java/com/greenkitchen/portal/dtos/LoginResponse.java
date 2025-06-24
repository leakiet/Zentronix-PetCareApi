package com.greenkitchen.portal.dtos;

import java.sql.Date;
import com.greenkitchen.portal.entities.Address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private String name;
    private String username;
    private String email;
    private Date birthDate;
	private String gender;
	private String phone;
    private Address address;
    private String token;
    private String tokenType;
}
