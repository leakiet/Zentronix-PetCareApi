package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.greenkitchen.portal.dtos.LoginRequest;
import com.greenkitchen.portal.dtos.LoginResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.utils.JwtUtils;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/apis/v1")
public class AuthController {
  @Autowired
  private CustomerService customerService;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    Customer customer = customerService.findByEmail(request.getEmail());

    if (customer != null && authentication.isAuthenticated()) {
      LoginResponse response = new LoginResponse();
      response.setName(customer.getName());
      response.setEmail(customer.getEmail());
      response.setUsername(customer.getUsername());
      response.setBirthDate(customer.getBirthDate());
      response.setGender(customer.getGender());
      response.setPhone(customer.getPhone());
      response.setTokenType("Bearer");
      response.setAddress(customer.getAddress());

      response.setToken(jwtUtils.generateJwtToken(authentication));

      return ResponseEntity.ok(response);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

}
