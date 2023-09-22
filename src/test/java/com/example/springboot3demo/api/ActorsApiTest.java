package com.example.springboot3demo.api;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.springboot3demo.domain.user.Actor;
import com.example.springboot3demo.domain.user.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ActorsApi.class)
public class ActorsApiTest {
  @MockBean
  private UserRepository userRepository;

  @Test
  public void should_create_user_success() {
    Actor actor = new Actor("Lufy", "MonkeyDMomoda");
    Map<String, Object> param = Map.of("username", actor.getUsername(), "displayName", actor.getDisplayName() );
    given().contentType(JSON).body(param).when().post("/actors").prettyPeek().then().statusCode(201);
  }

  @Test
  public void should_get_user_success() {
    when(userRepository.findByUsername(eq("none"))).thenReturn(Optional.empty());
    given().contentType(JSON).when().get("/actors/{username}", "none").then().statusCode(200);

  }
}