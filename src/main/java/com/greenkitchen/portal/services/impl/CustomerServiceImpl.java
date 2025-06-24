package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.repositories.CustomerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public Customer findByEmail(String email) {
    return customerRepository.findByEmail(email);
  }

  @Override
  public List<Customer> listAll() {
    return customerRepository.findAll();
  }

  @Override
  public Customer save(Customer customer) {
    String hashedPassword = encoder.encode(customer.getPassword());
    customer.setPassword(hashedPassword);
    return customerRepository.save(customer);
  }

  @Override
  public Customer findById(Long id) {
    return customerRepository.findById(id).orElse(null);
  }

  @Override
  public Customer update(Customer customer) {
    Customer existingCustomer = findById(customer.getId());
    if (existingCustomer == null) {
      throw new IllegalArgumentException("Customer not found with id: " + customer.getId());
    }
    existingCustomer.setName(customer.getName());
    existingCustomer.setEmail(customer.getEmail());
    existingCustomer.setUsername(customer.getUsername());
    existingCustomer.setBirthDate(customer.getBirthDate());
    existingCustomer.setGender(customer.getGender());
    existingCustomer.setPhone(customer.getPhone());
    existingCustomer.setAddress(customer.getAddress());

    return customerRepository.save(existingCustomer);
  }

  @Override
  public void deleteById(Long id) {
    customerRepository.deleteById(id);
  }

}
