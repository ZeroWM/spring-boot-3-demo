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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActorsApiTest {
  private static final String BASE_URI = "http://localhost:";
  @LocalServerPort private int port;

  @MockBean
  private UserRepository userRepository;

  @Test
  public void should_create_user_success() {
    Actor actor = new Actor("Lufy", "MonkeyDMomoda");
    Map<String, Object> param = Map.of("username", actor.getUsername(), "displayName", actor.getDisplayName() );
    given()
        .contentType(JSON)
        .body(param)
        .when()
        .post(BASE_URI + port + "/actors")
        .prettyPeek()
        .then()
        .statusCode(204);
  }


  @Test
  public void should_get_user_success() {
    when(userRepository.findByUsername(eq("none")))
        .thenReturn(Optional.of(new Actor("QiaoBa", "小狸猫")));

    given()
        .contentType("application/json")
        .header("Content-Type", "application/json")
        .when()
        .get(BASE_URI + port + "/actors/none")
        .then()
        .statusCode(200);
  }
}