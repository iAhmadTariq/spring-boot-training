package com.redmath.training.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ApiUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long userId;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Column(unique = true, nullable = false, length = 50)
  private String userName;

  @NotBlank(message = "Password is required")
  @Column(nullable = false)
  private String password;

  @NotBlank(message = "Roles are required")
  @Column(nullable = false)
  private String roles;

  @Column(unique = true)
  private String token;

  private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
  private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();
}
