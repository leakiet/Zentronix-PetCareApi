package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.Customer;

public interface CustomerService {
  Customer findByEmail(String email);
  List<Customer> listAll();
  Customer update(Customer customer);
  Customer save(Customer customer);
  Customer findById(Long id);
  void deleteById(Long id);
  Customer registerCustomer(Customer customer);
  Customer verifyEmail(String email, String verifyToken); 
}
