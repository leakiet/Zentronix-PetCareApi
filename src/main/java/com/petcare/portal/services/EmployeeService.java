package com.petcare.portal.services;

import com.petcare.portal.entities.Employee;

public interface EmployeeService {
  Employee findByEmail(String email);
}
