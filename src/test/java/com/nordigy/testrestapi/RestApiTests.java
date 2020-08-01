package com.nordigy.testrestapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
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
    public void shouldReturnUserIfExists() {
        ObjectNode user = given().log().all()
                .when().get("/api/users/10")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);
        assertThat(user.get("id").asInt()).isEqualTo(10);
        assertThat(user.get("email").asText()).isEqualTo("workingemail-10@gmail.com");
    }

    @Test
    public void shouldNotReturnUserIfNotExists() {
        given().log().all()
                .when().get("/api/users/30")
                .then().log().ifValidationFails()
                .statusCode(404);
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
    public void shouldCreateNewUserWhenName2Symbols() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "El");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "asdas@asdas.ru");

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
    public void shouldCreateNewUserWhenName15Symbols() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Алёна-Генриэтта");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "alyona@asdas.ru");

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
    public void shouldNotCreateNewUserWhenName1Symbol() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "I");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "alyona@asdas.ru");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.size", is(20));
    }

    @Test
    public void shouldNotCreateNewUserWhenName16Symbol() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ibhjllkjhgfdddrt");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "alyona@asdas.ru");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.size", is(20));
    }

    @Test
    public void shouldNotCreateNewUserWhenEmailInvalid() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "!*)(*&^54678jhgf");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
    }

    @Test
    public void shouldNotCreateNewUsersWhenSameEmail() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "blabla@asdas.ru");
        ObjectNode objectNodeOther = objectMapper.createObjectNode();
        objectNode.put("firstName", "Stepan");
        objectNode.put("lastName", "Petrov");
        objectNode.put("dayOfBirth", "2005-01-05");
        objectNode.put("email", "blabla@asdas.ru");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(201)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .body(objectNodeOther)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(21));
    }

    @Test
    public void shouldNotCreateNewUserWhenEmailAlreadyExists() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode user = given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/users/5")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", user.get("email"));

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(409)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
    }

    @Test
    public void shouldNotCreateNewUserWhenDateInvalidFormat() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000.01.01");
        objectNode.put("email", "kjhgh@asdas.ru");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.size", is(20));
    }

    @Test
    public void shouldNotCreateNewUserWhenDateInFuture() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "3000-01-01");
        objectNode.put("email", "kjhgh@asdas.ru");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);

        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.size", is(20));
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
    public void shouldFailPutNewInfoToExistingUserWhenSomeFieldsEmpty() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Mari");
        objectNode.put("lastName", "");
        objectNode.put("dayOfBirth", "");
        objectNode.put("email", "");

       given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().put("/api/users/2")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);
    }

//    @Test
//    public void shouldCreateNewUserWhenPutInfoToNotExistingUser() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode objectNode = objectMapper.createObjectNode();
//        objectNode.put("firstName", "Mari");
//        objectNode.put("lastName", "Brown");
//        objectNode.put("dayOfBirth", "1989-03-02");
//        objectNode.put("email", "mari@gjgj.com");
//
//        given().log().all()
//                .body(objectNode)
//                .contentType(ContentType.JSON)
//                .when().put("/api/users/51")
//                .then().log().ifValidationFails()
//                .statusCode(201)
//                .extract().body().as(ObjectNode.class);
    //.body("page.totalElements", is(21));
//    }

    @Test
    public void shouldPatchNewInfoToExistingUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ani");
        objectNode.put("email", "ani@asdas.ru");

        ObjectNode user = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().patch("/api/users/12")
                .then().log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(ObjectNode.class);

        assertThat(user.get("id").asInt()).isEqualTo(12);

    }

    @Test
    public void shouldFailPatchNewInfoToNotExistingUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Mari");
        objectNode.put("lastName", "");
        objectNode.put("dayOfBirth", "");
        objectNode.put("email", "");

        given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().patch("/api/users/55")
                .then().log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    public void shouldDeleteExistingUser() {
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
        given().log().all()
                .when().delete("/api/users/20")
                .then().log().ifValidationFails()
                .statusCode(204);
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(19));
    }

    @Test
    public void shouldDoNothingWhenDeleteNotExistingUser() {
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
        given().log().all()
                .when().delete("/api/users/40")
                .then().log().ifValidationFails()
                .statusCode(404);
        given().log().all()
                .when().get("/api/users")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("page.totalElements", is(20));
    }

    // TODO: The test methods above are examples of test cases.
    //  Please add new cases below, but don't hesitate to refactor the whole class.
}