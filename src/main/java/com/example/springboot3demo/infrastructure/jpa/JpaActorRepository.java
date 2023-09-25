package com.example.springboot3demo.infrastructure.jpa;

import com.example.springboot3demo.domain.actor.Actor;
import com.example.springboot3demo.domain.actor.ActorRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaActorRepository implements ActorRepository {
  private final JpaActorMapper actorMapper;

  @Override
  public Optional<Actor> findByUsername(String username) {
    return actorMapper.findById(username);
  }

  @Override
  public void save(Actor actor) {
    actorMapper.save(actor);
  }
}
