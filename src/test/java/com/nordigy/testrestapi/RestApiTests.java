package com.nordigy.testrestapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hibernate.criterion.Restrictions.or;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// It allows to refresh context(Database) before an each method. So your tests always will be executed on the same snapshot of DB.
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class RestApiTests {

    @LocalServerPort
    private int port;

    @PostConstruct
    public void init() {
        RestAssured.port = port;
    }

    @Test
    public void shouldReturnCorrectUsersListSize() {
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
    }

    @Test
    public void shouldCreateNewUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "asdas@asdas.tr");

        ObjectNode user = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(201)
                .extract().body().as(ObjectNode.class);

        assertThat(user.get("id").asLong()).isGreaterThan(20);
        assertThat(user.get("firstName")).isEqualTo(objectNode.get("firstName"));
        assertThat(user.get("lastName")).isEqualTo(objectNode.get("lastName"));
        assertThat(user.get("dayOfBirth")).isEqualTo(objectNode.get("dayOfBirth"));
        assertThat(user.get("email")).isEqualTo(objectNode.get("email"));
    }

    @Test
    public void shouldReturnUserIfExists() {
        ObjectNode user = given().log().all()
                .when().get("/api/users/10")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);
        assertThat(user.get("id").asInt()).isEqualTo(10);
    }

    @Test
    public void shouldPutNewInfoToExistingUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Julia");
        objectNode.put("lastName", "Smith");
        objectNode.put("dayOfBirth", "1967-01-01");
        objectNode.put("email", "juli@asdas.ru");

        ObjectNode user = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().put("/api/users/3")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);

        assertThat(user.get("id").asInt()).isEqualTo(3);
        assertThat(user.get("firstName")).isEqualTo(objectNode.get("firstName"));
        assertThat(user.get("lastName")).isEqualTo(objectNode.get("lastName"));
        assertThat(user.get("dayOfBirth")).isEqualTo(objectNode.get("dayOfBirth"));
        assertThat(user.get("email")).isEqualTo(objectNode.get("email"));
    }

    @Test
    public void shouldPatchNewInfoToExistingUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ani");
        objectNode.put("email", "ani@asdas.ru");

        ObjectNode userBeforePatch = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().get("/api/users/12")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);

        ObjectNode userAfterPatch = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().patch("/api/users/12")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);

        assertThat(userAfterPatch.get("id").asInt()).isEqualTo(12);
        assertThat(userAfterPatch.get("firstName")).isEqualTo(objectNode.get("firstName"));
        assertThat(userAfterPatch.get("lastName")).isEqualTo(userBeforePatch.get("lastName"));
        assertThat(userAfterPatch.get("dayOfBirth")).isEqualTo(userBeforePatch.get("dayOfBirth"));
        assertThat(userAfterPatch.get("email")).isEqualTo(objectNode.get("email"));
    }

    @Test
    public void shouldDeleteExistingUser() {
        given().log().all()
                .when().delete("/api/users/1")
                .then().log().ifValidationFails()
                .statusCode(204);
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(19));
    }

    // TODO: The test methods above are examples of test cases.
    //  Please add new cases below, but don't hesitate to refactor the whole class.
}