package com.petcare.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.dtos.EmailRequest;
import com.petcare.portal.dtos.GoogleLoginRequest;
import com.petcare.portal.dtos.LoginRequest;
import com.petcare.portal.dtos.LoginResponse;
import com.petcare.portal.dtos.RegisterRequest;
import com.petcare.portal.dtos.RegisterResponse;
import com.petcare.portal.dtos.ResetPasswordRequest;
import com.petcare.portal.dtos.VerifyRequest;
import com.petcare.portal.entities.User;
import com.petcare.portal.security.MyUserDetails;
import com.petcare.portal.security.MyUserDetailService;
import com.petcare.portal.services.UserService;
import com.petcare.portal.services.EmployeeService;
import com.petcare.portal.services.GoogleAuthService;
import com.petcare.portal.security.JwtUtils;

import jakarta.validation.Valid;

import com.petcare.portal.entities.Employee;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/apis/v1/auth")
public class AuthController {
  @Autowired
  private UserService userService;

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private MyUserDetailService userDetailService;

  @Autowired
  private ModelMapper mapper;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest request, HttpServletResponse httpResponse) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
    User user = userService.checkLogin(request.getEmail(), request.getPassword());
    LoginResponse response = mapper.map(user, LoginResponse.class);
    response.setToken(jwtUtils.generateJwtToken(authentication));
    response.setRefreshToken(jwtUtils.generateRefreshToken(authentication)); // Thêm refresh token
    response.setTokenType("Bearer");

    // Lưu access token vào cookie
    Cookie accessTokenCookie = new Cookie("access_token", response.getToken());
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
    httpResponse.addCookie(accessTokenCookie);

    // Lưu refresh token vào cookie
    Cookie refreshTokenCookie = new Cookie("refresh_token", response.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days (lâu hơn access token)
    httpResponse.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(response);

  }

  @DeleteMapping("/logout")
  public ResponseEntity<String> logoutCustomer(HttpServletResponse httpResponse) {
    // Xóa cookie access_token
    Cookie accessTokenCookie = new Cookie("access_token", null);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(0);
    httpResponse.addCookie(accessTokenCookie);

    // Xóa cookie refresh_token
    Cookie refreshTokenCookie = new Cookie("refresh_token", null);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(0);
    httpResponse.addCookie(refreshTokenCookie);

    return ResponseEntity.ok("Logout successful");
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
      response.setRefreshToken(jwtUtils.generateRefreshToken(authentication)); // Thêm refresh token cho employee
      response.setTokenType("Bearer");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerCustomer(@Valid @RequestBody RegisterRequest registerRequest) {
    User user = mapper.map(registerRequest, User.class);

    User registereddUser = userService.registerUser(user);

    RegisterResponse response = new RegisterResponse();
    response.setEmail(registereddUser.getEmail());
    response.setMessage("Register successfully");

    return ResponseEntity.ok(response);
  }

  @PutMapping("/verify")
  public ResponseEntity<String> verifyAccount(@RequestBody VerifyRequest request) {

    userService.verifyEmail(request.getEmail(), request.getToken());

    return ResponseEntity.ok("Email verified successfully");
  }

  @PutMapping("/resend-verifyEmail")
  public ResponseEntity<String> resendVerifyEmail(@RequestBody EmailRequest request) {
    userService.resendVerifyEmail(request.getEmail());
    return ResponseEntity.ok("Verification email resent!");
  }

  @PostMapping("/sendOtpCode")
  public ResponseEntity<String> sendOtpCode(@RequestBody EmailRequest request) {
    userService.sendOtpCode(request.getEmail());
    return ResponseEntity.ok("OTP code sent successfully");
  }

  @PostMapping("/verifyOtpCode")
  public ResponseEntity<String> verifyOtpCode(@RequestBody VerifyRequest request) {
    boolean isValid = userService.verifyOtpCode(request.getEmail(), request.getToken());
    if (!isValid) {
      throw new IllegalArgumentException("Invalid or expired OTP code");
    }
    return ResponseEntity.ok("OTP verified successfully");
  }
  

  @PostMapping("/resetPassword")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {  

    userService.resetPassword(request.getEmail(), request.getPassword());
    
    return ResponseEntity.ok("Password reset successfully. Please login with your new password.");
  }

  @PostMapping("/google-login")
  public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
    try {
      // Authenticate user với Google
      User user = googleAuthService.authenticateGoogleUser(request.getIdToken());
      
      // Tạo MyUserDetails object cho user
      MyUserDetails userDetails = new MyUserDetails(user);
      
      // Generate JWT token
      Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
      String jwt = jwtUtils.generateJwtToken(authentication);
      
      // Return response giống như login thường
      LoginResponse response = mapper.map(user, LoginResponse.class);
      response.setToken(jwt);
      response.setRefreshToken(jwtUtils.generateRefreshToken(authentication)); // Thêm refresh token
      response.setTokenType("Bearer");
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Google login failed: " + e.getMessage());
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse httpResponse) {
    try {
      // Lấy refresh token từ cookie
      String refreshToken = null;
      if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
          if ("refresh_token".equals(cookie.getName())) {
            refreshToken = cookie.getValue();
            break;
          }
        }
      }
      
      if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      
      // Validate refresh token
      if (!jwtUtils.validateRefreshToken(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      
      // Lấy username từ refresh token
      String username = jwtUtils.getUserNameFromRefreshToken(refreshToken);
      
      // Load user details
      MyUserDetails userDetails = (MyUserDetails) userDetailService.loadUserByUsername(username);
      
      // Tạo authentication object
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      
      // Generate new tokens
      String newAccessToken = jwtUtils.generateJwtToken(authentication);
      // Refresh token giữ nguyên, không tạo mới
      
      // Tạo response
      LoginResponse response = new LoginResponse();
      if (userDetails.getEmployee() != null) {
        response = mapper.map(userDetails.getEmployee(), LoginResponse.class);
      } else if (userDetails.getCustomer() != null) {
        response = mapper.map(userDetails.getCustomer(), LoginResponse.class);
      }
      
      response.setToken(newAccessToken);
      response.setRefreshToken(refreshToken); // Giữ nguyên refresh token cũ
      response.setTokenType("Bearer");
      
      // Chỉ cập nhật access token cookie, refresh token giữ nguyên
      Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
      httpResponse.addCookie(accessTokenCookie);
      // Không cập nhật refresh token cookie
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

}
