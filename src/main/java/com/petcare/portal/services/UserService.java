package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.entities.User;
import com.petcare.portal.dtos.userDtos.updateRequest;
import com.petcare.portal.dtos.userDtos.userResponse;

public interface UserService {
  User findByEmail(String email);
  List<User> listAll();
  User update(User user);
  User save(User user);
  User findById(Long id);
  void deleteById(Long id);
  User registerUser(User user);
  User verifyEmail(String email, String verifyToken);
  User resendVerifyEmail(String email);
  User checkLogin(String email, String password);
  void sendOtpCode(String email);
  boolean verifyOtpCode(String email, String otpCode);
  void resetPassword(String email, String newPassword);
  userResponse updateUserByEmail(updateRequest request);
}
