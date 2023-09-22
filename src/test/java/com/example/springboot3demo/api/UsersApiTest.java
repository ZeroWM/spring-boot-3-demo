package com.example.springboot3demo.api;

import com.example.springboot3demo.domain.user.User;
import com.example.springboot3demo.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(UsersApi.class)
public class UsersApiTest {
  @MockBean
  private UserRepository userRepository;

  @Test
  public void should_create_user_success() {

    User user = new User("name", "phone", "email");

    Map<String, Object> param = Map.of("username", user.getUsername(), "phone", user.getPhone(), "email", user.getEmail());

    given().contentType(JSON).body(param).when().post("/users").prettyPeek().then().statusCode(201);
  }

  @Test
  public void should_get_user_success() {
    when(userRepository.findByUsername(eq("none"))).thenReturn(Optional.empty());
    given().contentType(JSON).when().get("/users/{username}", "none").then().statusCode(200);

  }
}