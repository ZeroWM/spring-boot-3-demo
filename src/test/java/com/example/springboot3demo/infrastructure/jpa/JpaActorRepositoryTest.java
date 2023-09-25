package com.example.springboot3demo.infrastructure.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.example.springboot3demo.domain.actor.Actor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class JpaActorRepositoryTest {
  @Autowired private JpaActorMapper userMapper;

  @Test
  public void should_get_user_success() {
    Actor actor = new Actor("Nami", "小贼猫");
    userMapper.save(actor);
    Actor result = userMapper.findById(actor.getUsername()).get();
    assertThat(result.getUsername(), is(actor.getUsername()));
  }

  @Test
  public void should_save_user_success() {
    Actor actor = new Actor("SuoLong", "绿藻头");
    userMapper.save(actor);
    Actor result = userMapper.findById(actor.getUsername()).get();
    assertThat(result.getUsername(), is(actor.getUsername()));
  }
}
