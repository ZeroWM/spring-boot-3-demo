package com.example.springboot3demo.api;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.springboot3demo.domain.user.Actor;
import com.example.springboot3demo.domain.user.ActorRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ActorsApiTest {

  @MockBean private ActorRepository userRepository;

  @Test
  public void should_create_user_success() {
    Actor actor = new Actor("Lufy", "MonkeyDMomoda");
    Map<String, Object> param = Map.of("username", actor.getUsername(), "displayName", actor.getDisplayName() );
    given()
        .contentType(JSON)
        .body(param)
        .when()
        .post("/actors")
        .prettyPeek()
        .then()
        .statusCode(204);
  }


  @Test
  public void should_get_user_success() {
    when(userRepository.findByUsername(eq("none")))
        .thenReturn(Optional.of(new Actor("QiaoBa", "小狸猫")));

    given().contentType(JSON).when().get("/actors/none").prettyPeek().then().statusCode(200);
  }
}