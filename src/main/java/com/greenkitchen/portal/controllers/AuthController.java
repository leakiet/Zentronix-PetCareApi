package com.greenkitchen.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.LoginRequest;
import com.greenkitchen.portal.dtos.LoginResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.EmployeeService;
import com.greenkitchen.portal.utils.JwtUtils;
import com.greenkitchen.portal.entities.Employee;

import ch.qos.logback.core.model.Model;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;

@RestController
@RequestMapping("/apis/v1")
public class AuthController {
  @Autowired
  private CustomerService customerService;

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ModelMapper mapper;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      Customer customer = customerService.findByEmail(request.getEmail());
      if (customer != null && authentication.isAuthenticated()) {
        LoginResponse response = mapper.map(customer, LoginResponse.class);
        response.setRole("USER");
        response.setToken(jwtUtils.generateJwtToken(authentication));
        response.setTokenType("Bearer");
        return ResponseEntity.ok(response);
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Collections.singletonMap("errorMessage", "Username or password is incorrect"));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Collections.singletonMap("errorMessage", "Username or password is incorrect"));
    }
  }

  @PostMapping("/employee/login")
  public ResponseEntity<?> employeeLogin(@RequestBody LoginRequest request) {
    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      Employee employee = employeeService.findByEmail(request.getEmail());
      if (employee != null && authentication.isAuthenticated()) {
        LoginResponse response = mapper.map(employee, LoginResponse.class);
        response.setToken(jwtUtils.generateJwtToken(authentication));
        response.setTokenType("Bearer");
        return ResponseEntity.ok(response);
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Collections.singletonMap("errorMessage", "Username or password is incorrect"));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Collections.singletonMap("errorMessage", "Username or password is incorrect"));
    }
  }



}

