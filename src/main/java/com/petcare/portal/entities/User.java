package com.petcare.portal.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
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
@Table(name = "users")
public class User extends AbstractEntity {

	private String firstName;

	private String lastName;

	private String companyName;

	@JsonIgnore
	private String password;

	private String phone;

	@Email
	@NotBlank(message = "Email is required")
	@Column(unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	private Gender gender = Gender.UNDEFINED;

	@Enumerated(EnumType.STRING)
	private Role role = Role.UNDEFINED;

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

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<Pet> pets;

}
