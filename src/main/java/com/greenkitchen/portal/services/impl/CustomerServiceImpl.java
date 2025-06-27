package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.repositories.CustomerRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.greenkitchen.portal.services.EmailService;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private CustomerRepository customerRepository;
  
  @Autowired
  private EmailService emailService;

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
    existingCustomer.setFirstName(customer.getFirstName());
    existingCustomer.setLastName(customer.getLastName());
    existingCustomer.setEmail(customer.getEmail());
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

  @Override
  public Customer registerCustomer(Customer customer) {
    Customer existingCustomer = customerRepository.findByEmail(customer.getEmail());
    if (existingCustomer != null) {
      throw new IllegalArgumentException("Email already registered: " + customer.getEmail());
    }
    customer.setPassword(encoder.encode(customer.getPassword()));
    String verifyToken = UUID.randomUUID().toString();
    customer.setVerifyToken(verifyToken);
    
    Customer savedCustomer = customerRepository.save(customer);
    
    // Send verification email
    emailService.sendVerificationEmail(
        customer.getEmail(),
        verifyToken
    );
    
    return savedCustomer;
  }

  @Override
  public Customer verifyEmail(String email, String token) {
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null ){
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }

    if (customer.getIsActive()) {
      throw new IllegalArgumentException("Email already verified");
    }

    if (!customer.getVerifyToken().equals(token)) {
      throw new IllegalArgumentException("Invalid or expired verification token");
    }
    
    customer.setIsActive(true);
    customer.setVerifyToken(null);
    customerRepository.save(customer);
    return customer;
  }

}
