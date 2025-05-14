package com.smart.entities;

import java.util.ArrayList;


import java.util.List;

import javax.validation.constraints.Pattern;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "USER")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@NotBlank(message = "User name can not be empty!!")
	@Size(min = 3, max = 12, message = "User name must be between 3 to 12 !!")
	private String name;

	@Column(unique = true)
	@Email(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$", message = "Invalid Email !!")
	private String email;
	
	@NotBlank(message = "Password can not be null!!")
	@Size(min = 8, max = 100, message = "Password must be between 8 to 20 characters!!")              
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,20}$",
	         message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character (@#$%^&+=)!")
	private String password;
	
	private String role;
	private boolean enabled;
	private String imageUrl;

	@Column(length = 500)
	private String about;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)  // fetch = FetchType.LAZY,
	private List<Contact> contacts = new ArrayList<>();

}
