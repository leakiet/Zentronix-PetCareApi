package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired; 
import com.greenkitchen.portal.entities.Employee;
import com.greenkitchen.portal.repositories.EmployeeRepository;
import com.greenkitchen.portal.services.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;

  @Override
  public Employee findByEmail(String email) {
    return employeeRepository.findByEmail(email);
  }
}
