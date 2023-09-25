package com.example.springboot3demo.infrastructure.mybatis;

import com.example.springboot3demo.domain.actor.Actor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface ActorMapper {

  Actor findByUsername(@Param("username") String username);

  void insertActor(Actor actor);
}
