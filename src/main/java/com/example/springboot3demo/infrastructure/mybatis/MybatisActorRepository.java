package com.example.springboot3demo.infrastructure.mybatis;

import com.example.springboot3demo.domain.actor.Actor;
import com.example.springboot3demo.domain.actor.ActorRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class MybatisActorRepository implements ActorRepository {
  @Autowired private ActorMapper actorMapper;

  @Override
  public Optional<Actor> findByUsername(String username) {
    return Optional.of(actorMapper.findByUsername(username));
  }

  @Override
  public void save(Actor actor) {
    actorMapper.insertActor(actor);
  }
}
