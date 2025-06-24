package com.greenkitchen.portal.entities;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customer extends AbstractEntity {

	private String firstName;
	private String lastName;
	@JsonIgnore
	private String password;

	private String email;

	private Date birthDate;

	private String gender;

	private String phone;

	private Boolean isActive = false;

	private Boolean isVerified = false;

	private String verificationCode;

	@Embedded
	private Address address;

}
