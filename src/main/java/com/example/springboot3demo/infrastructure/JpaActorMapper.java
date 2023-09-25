package com.example.springboot3demo.infrastructure;

import com.example.springboot3demo.domain.user.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaActorMapper extends JpaRepository<Actor, String> {
}
