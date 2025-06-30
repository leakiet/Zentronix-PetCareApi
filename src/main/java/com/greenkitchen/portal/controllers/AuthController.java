package com.greenkitchen.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.EmailRequest;
import com.greenkitchen.portal.dtos.GoogleLoginRequest;
import com.greenkitchen.portal.dtos.LoginRequest;
import com.greenkitchen.portal.dtos.LoginResponse;
import com.greenkitchen.portal.dtos.RegisterRequest;
import com.greenkitchen.portal.dtos.RegisterResponse;
import com.greenkitchen.portal.dtos.ResetPasswordRequest;
import com.greenkitchen.portal.dtos.VerifyRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.security.MyUserDetails;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.EmployeeService;
import com.greenkitchen.portal.services.GoogleAuthService;
import com.greenkitchen.portal.utils.JwtUtils;

import jakarta.validation.Valid;

import com.greenkitchen.portal.entities.Employee;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/apis/v1")
public class AuthController {
  @Autowired
  private CustomerService customerService;

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private ModelMapper mapper;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest request) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
    Customer customer = customerService.checkLogin(request.getEmail(), request.getPassword());
    LoginResponse response = mapper.map(customer, LoginResponse.class);
    response.setRole("USER");
    response.setToken(jwtUtils.generateJwtToken(authentication));
    response.setTokenType("Bearer");
    return ResponseEntity.ok(response);

  }

  @PostMapping("/employee/login")
  public ResponseEntity<LoginResponse> employeeLogin(@RequestBody LoginRequest request) {
    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      Employee employee = employeeService.findByEmail(request.getEmail());
      if (employee == null || !authentication.isAuthenticated()) {
        throw new IllegalArgumentException("Username or password is incorrect");
      }
      LoginResponse response = mapper.map(employee, LoginResponse.class);
      response.setToken(jwtUtils.generateJwtToken(authentication));
      response.setTokenType("Bearer");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerCustomer(@Valid @RequestBody RegisterRequest registerRequest) {
    Customer customer = mapper.map(registerRequest, Customer.class);

    Customer registereddCustomer = customerService.registerCustomer(customer);

    RegisterResponse response = new RegisterResponse();
    response.setEmail(registereddCustomer.getEmail());
    response.setMessage("Register successfully");

    return ResponseEntity.ok(response);
  }

  @PutMapping("/verify")
  public ResponseEntity<String> verifyAccount(@RequestBody VerifyRequest request) {

    customerService.verifyEmail(request.getEmail(), request.getToken());

    return ResponseEntity.ok("Email verified successfully");
  }

  @PutMapping("/resend-verifyEmail")
  public ResponseEntity<String> resendVerifyEmail(@RequestBody EmailRequest request) {
    customerService.resendVerifyEmail(request.getEmail());
    return ResponseEntity.ok("Verification email resent!");
  }

  @PostMapping("/sendOtpCode")
  public ResponseEntity<String> sendOtpCode(@RequestBody EmailRequest request) {
    customerService.sendOtpCode(request.getEmail());
    return ResponseEntity.ok("OTP code sent successfully");
  }

  @PostMapping("/verifyOtpCode")
  public ResponseEntity<String> verifyOtpCode(@RequestBody VerifyRequest request) {
    boolean isValid = customerService.verifyOtpCode(request.getEmail(), request.getToken());
    if (!isValid) {
      throw new IllegalArgumentException("Invalid or expired OTP code");
    }
    return ResponseEntity.ok("OTP verified successfully");
  }
  

  @PostMapping("/resetPassword")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {  

    customerService.resetPassword(request.getEmail(), request.getPassword());
    
    return ResponseEntity.ok("Password reset successfully. Please login with your new password.");
  }

  @PostMapping("/google-login")
  public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
    try {
      // Authenticate user với Google
      Customer customer = googleAuthService.authenticateGoogleUser(request.getIdToken());
      
      // Tạo MyUserDetails object cho customer
      MyUserDetails userDetails = new MyUserDetails(customer);
      
      // Generate JWT token
      Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
      String jwt = jwtUtils.generateJwtToken(authentication);
      
      // Return response giống như login thường
      LoginResponse response = mapper.map(customer, LoginResponse.class);
      response.setRole("USER");
      response.setToken(jwt);
      response.setTokenType("Bearer");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Google login failed: " + e.getMessage());
    }
  }

}
