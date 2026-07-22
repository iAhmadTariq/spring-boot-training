package com.redmath.training.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ApiUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long userId;

  private String userName;
  private String password;
  private String roles;
  private String token;
  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt = LocalDateTime.now();
}
