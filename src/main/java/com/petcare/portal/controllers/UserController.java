package com.petcare.portal.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.entities.User;
import com.petcare.portal.services.UserService;

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
  @GetMapping
  public ResponseEntity<List<User>> getAllVets(@RequestParam(value = "role", required = false) String role) {
      try {
          List<User> users;
          if ("VET".equalsIgnoreCase(role)) {
              users = userService.findByRole("VET");
          } else {
              users = userService.findAll(); // Return all users if no role filter
          }

          if (users.isEmpty()) {
              return ResponseEntity.noContent().build();
          }

          return ResponseEntity.ok(users);
      } catch (Exception e) {
          return ResponseEntity.badRequest().build();
      }
  }
  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
      try {
          User user = userService.findById(id);

          if (user == null) {
              return ResponseEntity.notFound().build();
          }

          return ResponseEntity.ok(user);
      } catch (Exception e) {
          return ResponseEntity.badRequest().build();
      }
  }

}
