package com.example.springboot3demo.domain.user;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findByUsername(String username);

  void save(User user);
}
