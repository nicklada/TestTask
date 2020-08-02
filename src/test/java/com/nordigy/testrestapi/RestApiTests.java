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

    @Nested
    public class GetMethodTests {

        @Test
        public void shouldReturnCorrectUsersListSize() {
            given().log().all()
                    .when().get("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("page.totalElements", is(20));
        }

        @Test
        public void shouldReturnCorrectPageSize() {
            given().log().all()
                    .when().get("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("page.size", is(20));
        }

        @Test
        public void shouldReturnCorrectNumberOfPages() {
            given().log().all()
                    .when().get("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("page.totalPages", is(1));
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
        public void shouldNotReturnUserIfNotExists() {
            given().log().all()
                    .when().get("/api/users/30")
                    .then().log().ifValidationFails()
                    .statusCode(404);
        }

        @Test
        public void shouldSortByFirstNameForward() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Aaaaaaaaaaaaaaa");
            objectNode.put("lastName", "Ив");
            objectNode.put("dayOfBirth", "2000-01-01");
            objectNode.put("email", "asdas@asdas.tr");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(201);

            given().log().all()
                    .when().get("/api/users/?sort=firstName,asc")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body(("_embedded.users.firstName.get(0)"), is("Aaaaaaaaaaaaaaa"));
        }

        @Test
        public void shouldSortByFirstNameBackward() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Zzzzzzzzzzzzzzz");
            objectNode.put("lastName", "Киселёв-Лопатин");
            objectNode.put("dayOfBirth", "2000-01-01");
            objectNode.put("email", "zzz@asdas.tr");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(201);

            given().log().all()
                    .when().get("/api/users/?sort=firstName,desc")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body(("_embedded.users.firstName.get(0)"), is("Zzzzzzzzzzzzzzz"));
        }
    }

    @Nested
    public class PostMethodTests {

        @Test
        public void shouldNotCreateNewUserWhenEmptyFields() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "");
            objectNode.put("lastName", "");
            objectNode.put("dayOfBirth", "");
            objectNode.put("email", "");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/cars")
                    .then().log().ifValidationFails()
                    .statusCode(404)
                    .body("error", is("Not Found"));
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
                    .body("subErrors.get(0).message", is("размер должен находиться в диапазоне от 2 до 15"));
        }

        @Test
        public void shouldNotCreateNewUserWhenName16Symbols() {
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
                    .body("subErrors.get(0).message", is("размер должен находиться в диапазоне от 2 до 15"));
        }

        @Test
        public void shouldNotCreateNewUserWhenSurname1Symbol() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Ivan");
            objectNode.put("lastName", "I");
            objectNode.put("dayOfBirth", "2000-01-01");
            objectNode.put("email", "alyona@asdas.ru");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(400)
                    .body("subErrors.get(0).message", is("размер должен находиться в диапазоне от 2 до 30"));
        }

        @Test
        public void shouldNotCreateNewUserWhenSurname31Symbols() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Ivan");
            objectNode.put("lastName", "Киселёв-ЛопатинКиселёв-Лопатины");
            objectNode.put("dayOfBirth", "2000-01-01");
            objectNode.put("email", "alyona@asdas.ru");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(400)
                    .body("subErrors.get(0).message", is("размер должен находиться в диапазоне от 2 до 30"));
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
                    .body("subErrors.get(0).message", is("должно иметь формат адреса электронной почты"));
        }

        @Test
        public void shouldNotCreateNewUserWhenEmailAlreadyExists() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode existingUser = given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/api/users/5")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .extract().body().as(ObjectNode.class);

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Ivan");
            objectNode.put("lastName", "Ivanov");
            objectNode.put("dayOfBirth", "2000-01-01");
            objectNode.put("email", existingUser.get("email"));

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().post("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(409)
                    .body("message", is("Database error"))
                    .body("debugMessage", containsStringIgnoringCase("Нарушение уникального индекса или первичного ключа"));
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
                    .body("message", is("Wrong content-type of the request format. Expected content-type is application/json."))
                    .body("debugMessage", containsStringIgnoringCase("JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String"));
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
                    .body("message", is("Validation failed"))
                    .body("subErrors.get(0).message", is("должно содержать прошедшую дату"));
        }
    }

    @Nested
    public class PutMethodTests {

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
        public void shouldNotPutNewInfoToExistingUserWhenSomeFieldsEmpty() {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("firstName", "Mari");
            objectNode.put("lastName", "Ldjkgk");
            objectNode.put("dayOfBirth", "1987-08-05");
            objectNode.put("email", "");

            given().log().all()
                    .body(objectNode)
                    .contentType(ContentType.JSON)
                    .when().put("/api/users/2")
                    .then().log().ifValidationFails()
                    .statusCode(400)
                    .body("message", is("Validation failed"))
                    .body("subErrors.get(0).message", is("не должно быть пустым"));
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
//                .body("page.totalElements", is(21));
//    }
    }

    @Nested
    public class PatchMethodTests {

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
        public void shouldNotPatchNewInfoToNotExistingUser() {
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
    }

    @Nested
    public class DeleteMethodTests {

        @Test
        public void shouldDeleteExistingUser() {
            given().log().all()
                    .when().delete("/api/users/1")
                    .then().log().ifValidationFails()
                    .statusCode(204)
                    .extract().body();
            given().log().all()
                    .when().get("/api/users")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .body("page.totalElements", is(19));
        }

        @Test
        public void shouldDoNothingWhenDeleteNotExistingUser() {
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
    }

    // TODO: The test methods above are examples of test cases.
    //  Please add new cases below, but don't hesitate to refactor the whole class.
}