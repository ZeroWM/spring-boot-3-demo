package com.example.springboot3demo.domain.user;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "actors")
@NoArgsConstructor
public class Actor {
  @Id
  private String username;
  private String displayName;

  public Actor(String username, String displayName) {
    this.username = username;
    this.displayName = displayName;
  }
}
