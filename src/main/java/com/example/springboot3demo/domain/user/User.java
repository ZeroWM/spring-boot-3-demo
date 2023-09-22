package com.example.springboot3demo.domain.user;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
  @Id
  private String username;
  private String phone;
  private String email;

  public User(String username, String phone, String email) {
    this.username = username;
    this.phone = phone;
    this.email = email;
  }
}
