package com.petcare.portal.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.petcare.portal.entities.User;
import com.petcare.portal.entities.Employee;
import com.petcare.portal.services.UserService;
import com.petcare.portal.services.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {
	private final UserService customerService;
	private final EmployeeService employeeService;

	public MyUserDetailService(UserService customerService, EmployeeService employeeService) {
		this.customerService = customerService;
		this.employeeService = employeeService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User customer = customerService.findByEmail(username);
		if (customer != null) {
			return new MyUserDetails(customer);
		}
		Employee employee = employeeService.findByEmail(username);
		if (employee != null) {
			return new MyUserDetails(employee);
		}
		throw new UsernameNotFoundException("User not found with email: " + username);
	}
}
