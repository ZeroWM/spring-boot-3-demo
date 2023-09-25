package com.example.springboot3demo.api;

import com.example.springboot3demo.domain.actor.Actor;
import com.example.springboot3demo.domain.actor.ActorRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actors")
@RequiredArgsConstructor
public class ActorsApi {
  private final ActorRepository userRepository;

  @PostMapping
  @Transactional
  public ResponseEntity<Actor> createUser(@RequestBody NewActorParameter param) {
    Actor actor = new Actor(param.getUsername(), param.getDisplayName());
    userRepository.save(actor);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{username}")
  public ResponseEntity<Actor> getUser(@PathVariable(value = "username") String username) {
    return ResponseEntity.ok(userRepository.findByUsername(username).get());
  }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class NewActorParameter {
  private String username;
  private String displayName;
}
