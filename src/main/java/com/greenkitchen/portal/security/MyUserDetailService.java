package com.greenkitchen.portal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.services.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {
	@Autowired
	private CustomerService customerService;
	
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	  Customer customer = customerService.findByEmail(username);
	  return new MyUserDetails(customer);
  }
}
