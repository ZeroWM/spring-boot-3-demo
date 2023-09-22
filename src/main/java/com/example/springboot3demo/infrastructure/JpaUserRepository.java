package com.example.springboot3demo.infrastructure;

import com.example.springboot3demo.domain.user.Actor;
import com.example.springboot3demo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {
  private final JpaUserMapper userMapper;

  @Override
  public Optional<Actor> findByUsername(String username) {
    return userMapper.findById(username);
  }

  @Override
  public void save(Actor actor) {
    userMapper.save(actor);
  }
}
