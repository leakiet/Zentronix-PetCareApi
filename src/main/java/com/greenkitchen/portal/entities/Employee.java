package com.greenkitchen.portal.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
public class Employee extends AbstractEntity {
  private String username;
	private String firstName;
  private String lastName;
  @JsonIgnore
  private String password;
  private String email;
  private String phone;
  private Boolean isActive = true;
  private String role;

}
