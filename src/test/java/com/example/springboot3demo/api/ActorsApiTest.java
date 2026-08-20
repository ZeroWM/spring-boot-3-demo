package com.example.springboot3demo.api;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.springboot3demo.domain.actor.Actor;
import com.example.springboot3demo.domain.actor.ActorRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ActorsApiTest {

  @MockBean private ActorRepository userRepository;

  @Test
  public void should_create_user_success() {
    Map<String, Object> param =
        Map.of("username", "Lufy", "displayName", "MonkeyDMomoda");

    given()
        .contentType(JSON)
        .body(param)
        .when()
        .post("/actors")
        .prettyPeek()
        .then()
        .statusCode(204);

    ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
    verify(userRepository).save(actorCaptor.capture());
    assertThat(actorCaptor.getValue().getUsername()).isEqualTo("Lufy");
    assertThat(actorCaptor.getValue().getDisplayName()).isEqualTo("MonkeyDMomoda");
  }


  @Test
  public void should_get_user_success() {
    when(userRepository.findByUsername(eq("none")))
        .thenReturn(Optional.of(new Actor("QiaoBa", "小狸猫")));

    given().contentType(JSON).when().get("/actors/none").prettyPeek().then().statusCode(200);
  }
}
