package com.example.springboot3demo.domain.actor;

import java.util.Optional;

public interface ActorRepository {
  Optional<Actor> findByUsername(String username);

  void save(Actor actor);
}
