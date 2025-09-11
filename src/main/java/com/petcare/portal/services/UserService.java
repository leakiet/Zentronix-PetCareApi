package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.entities.User;

public interface UserService {
  User findByEmail(String email);
  List<User> listAll();
  User update(User customer);
  User save(User customer);
  User findById(Long id);
  void deleteById(Long id);
  User registerUser(User user);
  User verifyEmail(String email, String verifyToken);
  User resendVerifyEmail(String email);
  User checkLogin(String email, String password);
  void sendOtpCode(String email);
  boolean verifyOtpCode(String email, String otpCode);
  void resetPassword(String email, String newPassword);
}
