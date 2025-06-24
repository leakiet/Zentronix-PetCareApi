package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.Employee;

public interface EmployeeService {
  Employee findByEmail(String email);
}
