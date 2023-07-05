/*
 * dummy-json
 * Copyright (c) 2023 Duberly Guarnizo Fuentes Rivera <duberlygfr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duberlyguarnizo.dummyjson.jsoncontent.controller;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContentRepository;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenService;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JCManagementControllerTest {
    static private String superJwt;
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    private static final List<Long> idList = new ArrayList<>(); //container for id's of originally created managers
    static Faker faker = new Faker();
    static private String adminJwt;
    @Autowired
    JsonContentRepository methodJcRepository;
    static private String clientJwt;

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }

    @BeforeAll
    public static void setUp(@Autowired JwtUtil jwtUtil,
                             @Autowired JwtTokenService tokenService,
                             @Autowired AppUserRepository userRepository,
                             @Autowired JsonContentRepository setUpJcRepository,
                             @Autowired WebApplicationContext context,
                             @Autowired PasswordEncoder pwEncoder) {
        //create an ADMIN user
        AppUser adminUser = userRepository.save(AppUser.builder()
                .id(1L)
                .names("admin user")
                .email("adminemail@admin.com")
                .username("admin")
                .password(pwEncoder.encode("pass"))
                .idCard("12345678")
                .role(AppUserRole.ADMIN)
                .active(true)
                .locked(false)
                .build()
        );
        AppUser supervisorUser = userRepository.save(AppUser.builder()
                .id(2L)
                .names("supervisor user")
                .email("supervisor@supervisor.com")
                .username("supervisor")
                .password(pwEncoder.encode("pass"))
                .idCard("987654321")
                .role(AppUserRole.SUPERVISOR)
                .active(true)
                .locked(false)
                .build()
        );

        //create an AppUser with role USER
        AppUser clientUser = userRepository.save(AppUser.builder()
                .id(3L)
                .names("client user")
                .email("clientmail@client.com")
                .username("client")
                .password(pwEncoder.encode("pass"))
                .idCard("98765432")
                .role(AppUserRole.USER)
                .active(true)
                .locked(false)
                .build()
        );
        adminJwt = jwtUtil.generateToken(adminUser);
        tokenService.saveToken(adminJwt, adminUser.getId());
        superJwt = jwtUtil.generateToken(supervisorUser);
        tokenService.saveToken(superJwt, supervisorUser.getId());
        clientJwt = jwtUtil.generateToken(clientUser);
        tokenService.saveToken(clientJwt, clientUser.getId());
        RestAssuredMockMvc.webAppContextSetup(context);

        String personJson = """
                {
                  "id": 1,
                  "name": "John Doe",
                  "age": 25,
                  "email": "johndoe@example.com",
                  "address": {
                    "street": "123 Main St",
                    "city": "Springfield"
                  }
                }""";

        String productJson = """
                {
                  "productId": "P123",
                  "productName": "Desktop Computer",
                  "price": 799.99,
                  "inStock": true,
                  "specifications": {
                    "brand": "XYZ Co."
                  }
                }""";

        String bookJson = """
                {
                  "bookId": "B789",
                  "title": "Example Book",
                  "author": "John Smith",
                  "publishedYear": 2001,
                  "genres": ["Mystery", "Thriller"]
                }""";

        //Create some JsonContent
        JsonContent jc1 = JsonContent.builder()
                .id(1L)
                .name("foreign client")
                .json(personJson)
                .path("/json/1/foreign-client")
                .build();

        JsonContent jc2 = JsonContent.builder()
                .id(2L)
                .name("pharmacy product")
                .json(productJson)
                .path("/json/2/pharmacy-product")
                .build();

        JsonContent jc3 = JsonContent.builder()
                .id(3L)
                .name("books example")
                .json(bookJson)
                .path("/json/3/books-example")
                .build();

        setUpJcRepository.deleteAll(); //empty the repo before, in case some info remains
        jc1.setCreatedBy(2L);
        jc1.setCreatedBy(2L);
        jc1.setCreatedBy(2L);
        idList.add(setUpJcRepository.save(jc1).getId());
        idList.add(setUpJcRepository.save(jc2).getId());
        idList.add(setUpJcRepository.save(jc3).getId());
    }

    @Test
    @Order(0)
    void getJCByInvalidIdReturnsError() {
        Long notExistingId = 999L;
        String invalidFormatId = "notAnId";
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", notExistingId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", invalidFormatId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @Order(1)
    void getJCWithValidIdReturnOk() {
        Long existingId1 = idList.get(0);
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", existingId1)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(existingId1.intValue()));
        Long existingId2 = idList.get(1);
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", existingId2)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(existingId2.intValue()));

    }

    @Test
    @Order(2)
    void getJCWithValidIdReturnsCorrectSchema() {
        Long existingId1 = idList.get(0);
        String jsonSchema = """
                {
                  "title": "JsonContentDetailDto",
                  "type": "object",
                  "properties": {
                    "createdBy": {
                      "type": "number"
                    },
                    "modifiedBy": {
                      "type": ["number", "null"]
                    },
                    "createdDate": {
                      "type": "string",
                      "format": "date-time"
                    },
                    "modifiedDate": {
                      "type": "string",
                      "format": "date-time"
                    },
                    "id": {
                      "type": "number"
                    },
                    "name": {
                      "type": "string",
                      "minLength": 1
                    },
                    "json": {
                      "type": "string",
                      "minLength": 3,
                      "maxLength": 2048
                    },
                    "path": {
                      "type": "string"
                    }
                  },
                  "required": ["name", "json"]
                }
                """;
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "en")
                .when()
                .get("/api/v1/management/json/{id}", existingId1)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(existingId1.intValue()))
                .assertThat().body(matchesJsonSchema(jsonSchema));

    }

    @Test
    @Order(3)
    void getJCWithInvalidRoleReturnsError() {
        //TODO: try this on postman to verify ProblemDetails body
        Long existingId = idList.get(0);
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", existingId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Order(4)
    void getJCByUserIdAndUserExistsAndHasJsonContent() {
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/by-user/{id}", 2)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content.size()", greaterThan(0))
                .body("empty", equalTo(false));
    }

    @Test
    @Order(5)
    void getJCByUserIdAndUserExistsAndNoJsonContent() {
        // Assuming id 3 has no JsonContent
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/by-user/{id}", 3)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content", is(empty()))
                .body("empty", equalTo(true));
    }

    @Test
    @Order(6)
    void getJCByUserIdAndUserDoesNotExist() {
        // Assuming id 100 does not exist
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/by-user/{id}", 100)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @Order(7)
    void getJCByUserIdWithIncorrectRole() {
        // Assuming id 100 does not exist
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/by-user/{id}", 100)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
        //.body("status", equalTo(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Order(8)
    void testJsonContentDataAvailable() {
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content.size()", greaterThan(0))
                .body("empty", equalTo(false));
    }


    @Test
    @Order(9)
    void getJcListWithIncorrectRoleReturnsError() {
        // You might need to delete all the JsonContent data before running this test
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
        //.body("status", equalTo(HttpStatus.FORBIDDEN.value()));
        ;
    }

    @Test
    @Order(10)
    void testUpdateExistingJsonContentWithValidData() {
        String jsonDto = """
                {
                	"id": 1,
                	"name": "changed name",
                	"json": "[{}]",
                	"path": "/json/1/changed-name"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .contentType(ContentType.JSON)
                .body(jsonDto)
                .when()
                .patch("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/{id}", 1)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(1))
                .body("name", equalTo("changed name"))
                .body("json", equalTo("[{}]"))
                .body("path", equalTo("/json/1/changed-name"));
    }

    @Test
    @Order(11)
    void testUpdateNonExistentJsonContent() {
        String jsonDto = """
                {
                	"id": 999,
                	"name": "changed name",
                	"json": "[{}]",
                	"path": "/json/999/nonexistent"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .contentType(ContentType.JSON)
                .body(jsonDto)
                .when()
                .patch("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(12)
    void testUpdateExistingJsonContentWithInvalidData() {
        String jsonDto = "{ 'id': 1 , 'name': '', 'json': '{}', 'path': '/json/1/invalid' }"; // Assuming 'name' cannot be empty

        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .contentType(ContentType.JSON)
                .body(jsonDto)
                .when()
                .patch("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(13)
    void testDeleteExistingJsonContent() {
        // Assuming id 1 exists
        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/management/json/{id}", 1)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json/{id}", 1)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @Order(14)
    void testDeleteNonExistentJsonContent() {
        // Assuming id 999 does not exist
        given()
                .header("Authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/management/json/{id}", 999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @Order(15)
    void testNoJsonContentData() {// last method, as it requires to empty the DB
        methodJcRepository.deleteAll();
        given()
                .log()
                .ifValidationFails()
                .header("Authorization", "Bearer " + superJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/management/json")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content", is(empty()))
                .body("empty", equalTo(true));
        ;
    }
}