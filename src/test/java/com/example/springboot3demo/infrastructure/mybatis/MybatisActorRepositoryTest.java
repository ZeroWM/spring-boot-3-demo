package com.example.springboot3demo.infrastructure.mybatis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.example.springboot3demo.domain.actor.Actor;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@MybatisTest
@Import({MybatisActorRepository.class})
public class MybatisActorRepositoryTest {

  @Autowired private MybatisActorRepository actorRepository;

  @Test
  public void should_get_user_success() {
    Actor actor = new Actor("Nami", "小贼猫");
    actorRepository.save(actor);
    Actor result = actorRepository.findByUsername(actor.getUsername()).get();
    assertThat(result.getUsername(), is(actor.getUsername()));
  }

  @Test
  public void should_save_user_success() {
    Actor actor = new Actor("SuoLong", "绿藻头");
    actorRepository.save(actor);
    Actor result = actorRepository.findByUsername(actor.getUsername()).get();
    assertThat(result.getUsername(), is(actor.getUsername()));
  }
}
