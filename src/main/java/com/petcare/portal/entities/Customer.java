package com.petcare.portal.entities;

import java.sql.Date;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petcare.portal.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

	@Email
	@NotBlank(message = "Email is required")
	@Column(unique = true)
	private String email;

	private Date birthDate;

	@Enumerated(EnumType.STRING)
	private Gender gender = Gender.UNDEFINED;

	@Column(unique = true)
	private String phone;

	private Boolean isActive = false;

	@JsonIgnore
	private String verifyToken;

	@JsonIgnore
	private LocalDateTime verifyTokenExpireAt;

	private String oauthProvider; // "google", null cho traditional users

	private String oauthProviderId; // ID từ Google

	private Boolean isOauthUser = false; // false cho traditional users

	@Embedded
	private Address address;

}
