package com.petcare.portal.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired; 
import com.petcare.portal.entities.Employee;
import com.petcare.portal.repositories.EmployeeRepository;
import com.petcare.portal.services.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;

  @Override
  public Employee findByEmail(String email) {
    return employeeRepository.findByEmail(email);
  }
}
