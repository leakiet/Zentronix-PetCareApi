package com.petcare.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.petcare.portal.entities.Customer;
import com.petcare.portal.services.CustomerService;

import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/apis/v1/customers")
public class CustomerController {

  @Autowired
  private CustomerService customerService;

  @Autowired
  private ModelMapper modelMapper;


  @GetMapping("/email/{email}")
  public ResponseEntity<Customer> getCustomerByEmail(@PathVariable("email") String email) {
    try {
      Customer customer = customerService.findByEmail(email);

      if (customer == null) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(customer);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }


}
