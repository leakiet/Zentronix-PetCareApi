package com.greenkitchen.portal.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import com.greenkitchen.portal.entities.Customer;

public class MyUserDetails implements UserDetails {

	private static final long serialVersionUID = 995564934432043084L;
	
	private Customer customer;
	
	public MyUserDetails(Customer customer) {
		this.customer = customer;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("USER"));
		return authorities;
	}

	@Override
	public String getPassword() {
		return customer.getPassword();
	}

	@Override
	public String getUsername() {
		return customer.getEmail();
	}

	public List<String> getRoles() {
		List<String> roles = new ArrayList<>();
		roles.add("USER");
		return roles;
	}

	
}
