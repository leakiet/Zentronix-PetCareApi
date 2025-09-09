package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.entities.Customer;

public interface CustomerService {
  Customer findByEmail(String email);
  List<Customer> listAll();
  Customer update(Customer customer);
  Customer save(Customer customer);
  Customer findById(Long id);
  void deleteById(Long id);
  Customer registerCustomer(Customer customer);
  Customer verifyEmail(String email, String verifyToken);
  Customer resendVerifyEmail(String email);
  Customer checkLogin(String email, String password);
  void sendOtpCode(String email);
  boolean verifyOtpCode(String email, String otpCode);
  void resetPassword(String email, String newPassword);
}
