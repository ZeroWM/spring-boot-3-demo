package com.example.springboot3demo.infrastructure;

import com.example.springboot3demo.domain.user.Actor;
import com.example.springboot3demo.domain.user.ActorRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaActorRepository implements ActorRepository {
  private final JpaActorMapper userMapper;

  @Override
  public Optional<Actor> findByUsername(String username) {
    return userMapper.findById(username);
  }

  @Override
  public void save(Actor actor) {
    userMapper.save(actor);
  }
}
