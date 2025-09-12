package com.petcare.portal.services.impl;

import org.springframework.stereotype.Service;

import com.petcare.portal.entities.User;
import com.petcare.portal.entities.OtpRecords;
import com.petcare.portal.services.UserService;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.repositories.OtpRecordsRepository;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.petcare.portal.services.EmailService;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository customerRepository;

  @Autowired
  private OtpRecordsRepository otpRecordsRepository;

  @Autowired
  private EmailService emailService;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public User findByEmail(String email) {
    return customerRepository.findByEmail(email);
  }

  @Override
  public List<User> listAll() {
    return customerRepository.findAll();
  }

  @Override
  public User save(User customer) {
    String hashedPassword = encoder.encode(customer.getPassword());
    customer.setPassword(hashedPassword);
    return customerRepository.save(customer);
  }

  @Override
  public User findById(Long id) {
    return customerRepository.findById(id).orElse(null);
  }

  @Override
  public User update(User customer) {
    User existingCustomer = findById(customer.getId());
    if (existingCustomer == null) {
      throw new IllegalArgumentException("Customer not found with id: " + customer.getId());
    }
    existingCustomer.setFirstName(customer.getFirstName());
    existingCustomer.setLastName(customer.getLastName());
    existingCustomer.setEmail(customer.getEmail());
    existingCustomer.setGender(customer.getGender());
    existingCustomer.setPhone(customer.getPhone());
    existingCustomer.setAddress(customer.getAddress());

    return customerRepository.save(existingCustomer);
  }

  @Override
  public void deleteById(Long id) {
    customerRepository.deleteById(id);
  }

  @Override
  public User checkLogin(String email, String password) {
    User customer = customerRepository.findByEmail(email);
    if (customer == null || !encoder.matches(password, customer.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    return customer;
  }

  @Override
  public User registerUser(User customer) {
    User existingCustomer = customerRepository.findByEmail(customer.getEmail());
    if (existingCustomer != null) {
      throw new IllegalArgumentException("Email already registered: " + customer.getEmail());
    }
    customer.setPassword(encoder.encode(customer.getPassword()));
    String verifyToken = UUID.randomUUID().toString();
    customer.setVerifyToken(verifyToken);
    customer.setVerifyTokenExpireAt(LocalDateTime.now().plusMinutes(10));
    User savedCustomer = customerRepository.save(customer);
    // Send verification email
    emailService.sendVerificationEmail(
        customer.getEmail(),
        verifyToken);
    return savedCustomer;
  }

  @Override
  public User verifyEmail(String email, String token) {
    User customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    if (customer.getIsActive()) {
      throw new IllegalArgumentException("Email already verified");
    }
    if (!customer.getVerifyToken().equals(token)) {
      throw new IllegalArgumentException("Invalid verification token");
    }
    if (customer.getVerifyTokenExpireAt() == null || LocalDateTime.now().isAfter(customer.getVerifyTokenExpireAt())) {
      throw new IllegalArgumentException("Verification token has expired");
    }
    customer.setIsActive(true);
    customer.setVerifyToken(null);
    customer.setVerifyTokenExpireAt(null);
    customerRepository.save(customer);
    return customer;
  }

  @Override
  public User resendVerifyEmail(String email) {
    User customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    String verifyToken = UUID.randomUUID().toString();
    customer.setVerifyToken(verifyToken);
    customer.setVerifyTokenExpireAt(LocalDateTime.now().plusMinutes(10));
    customerRepository.save(customer);
    emailService.sendVerificationEmail(
        customer.getEmail(),
        verifyToken
      );
    return customer;
  }

  @Override
  public void sendOtpCode(String email) {

    User customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Please verify your email first before requesting OTP");
    }
    if(customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Check if there's already a recent OTP request within 5 minutes
    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
    OtpRecords recentOtpRecord = otpRecordsRepository.findRecentOtpByEmailAndTime(email, fiveMinutesAgo);
    
    if (recentOtpRecord != null) {
      throw new IllegalArgumentException("You have requested OTP too frequently. Please try again later.");
    }

    // Generate 6-digit random OTP
    String otpCode = generateRandomOtp();
    // Set expiration time (5 minutes from now)
    LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
    // Create and save OTP record
    OtpRecords otpRecord = new OtpRecords();
    otpRecord.setEmail(email);
    otpRecord.setOtpCode(otpCode);
    otpRecord.setExpiredAt(expiredAt);
    otpRecordsRepository.save(otpRecord);
    
    // Send OTP via email
    emailService.sendOtpEmail(email, otpCode);
  }

  @Override
  public boolean verifyOtpCode(String email, String otpCode) {

    OtpRecords otpRecord = otpRecordsRepository.findByEmailAndOtpCode(email, otpCode);

    if (otpRecord == null) throw new IllegalArgumentException("Invalid OTP code");
    if (otpRecord.isExpired()) throw new IllegalArgumentException("OTP code has expired");
    if (otpRecord.getIsUsed()) throw new IllegalArgumentException("OTP code has already been used");
    // Mark OTP as used
    otpRecord.setIsUsed(true);
    otpRecordsRepository.save(otpRecord);
    
    return true;
  }

  @Override
  public void resetPassword(String email, String newPassword) {
    // Find customer
    User customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    
    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email first.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }
    
    // Update password
    String hashedPassword = encoder.encode(newPassword);
    customer.setPassword(hashedPassword);
    customerRepository.save(customer);
    
    // Mark all other OTPs as used for security
    otpRecordsRepository.markAllOtpAsUsedByEmail(email);
  }

  private String generateRandomOtp() {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
    return String.valueOf(otp);
  }
  @Override
  public List<User> findByRole(String role) {
    return customerRepository.findByRole(com.petcare.portal.enums.Role.valueOf(role.toUpperCase()));
  }

@Override
public List<User> findAll() {
	return customerRepository.findAll();
}
}
