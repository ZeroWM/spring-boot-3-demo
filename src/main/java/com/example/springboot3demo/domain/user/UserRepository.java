package com.example.springboot3demo.domain.user;

import java.util.Optional;

public interface UserRepository {
  Optional<Actor> findByUsername(String username);

  void save(Actor actor);
}
