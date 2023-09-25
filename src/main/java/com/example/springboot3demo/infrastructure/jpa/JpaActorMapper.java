package com.example.springboot3demo.infrastructure.jpa;

import com.example.springboot3demo.domain.actor.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaActorMapper extends JpaRepository<Actor, String> {
}
