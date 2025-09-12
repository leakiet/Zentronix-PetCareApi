package com.petcare.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.petcare.portal.entities.User;
import com.petcare.portal.services.UserService;
import com.petcare.portal.dtos.userDtos.updateRequest;
import com.petcare.portal.dtos.userDtos.userResponse;

import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/users")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private ModelMapper modelMapper;


  @GetMapping("/email/{email}")
  public ResponseEntity<User> getCustomerByEmail(@PathVariable("email") String email) {
    try {
      User user = userService.findByEmail(email);

      if (user == null) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/update")
  public ResponseEntity<userResponse> updateUser(@Valid @RequestBody updateRequest updateRequest) {
    try {
      userResponse response = userService.updateUserByEmail(updateRequest);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      userResponse errorResponse = new userResponse();
      errorResponse.setMessage(e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
  


}
