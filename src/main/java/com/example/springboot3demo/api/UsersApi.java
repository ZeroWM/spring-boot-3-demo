package com.example.springboot3demo.api;

import com.example.springboot3demo.domain.user.User;
import com.example.springboot3demo.domain.user.UserRepository;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersApi {
  private final UserRepository userRepository;

  @PostMapping
  @Transactional
  public ResponseEntity<User> createUser(@RequestBody NewUserParameter param) {
    User user = new User(param.getUsername(), param.getPhone(), param.getEmail());
    userRepository.save(user);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/{username}")
  public ResponseEntity<User> getUser(@PathVariable(value = "username") String username) {
    return ResponseEntity.ok(userRepository.findByUsername(username).get());
  }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class NewUserParameter {
  private String username;
  private String phone;
  private String email;
}
