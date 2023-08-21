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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JCAuthenticatedControllerTest {
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    private static final List<Long> jcIdList = new ArrayList<>(); //container for id's of originally created managers
    private static String superJwt;
    private static String adminJwt;
    private static String clientJwt;

    private static Long adminUserId;
    private static Long supervisorUserId;
    private static Long clientUserId;

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
        adminUserId = adminUser.getId();
        AppUser supervisorUser = userRepository.save(AppUser.builder()
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
        supervisorUserId = supervisorUser.getId();
        //create an AppUser with role USER
        AppUser clientUser = userRepository.save(AppUser.builder()
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
        clientUserId = clientUser.getId();
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
        jc1.setCreatedBy(clientUserId);
        jc2.setCreatedBy(clientUserId);
        jc3.setCreatedBy(clientUserId);
        jcIdList.add(setUpJcRepository.save(jc1).getId());
        jcIdList.add(setUpJcRepository.save(jc2).getId());
        jcIdList.add(setUpJcRepository.save(jc3).getId());
    }

    @Test
    @DisplayName("Get JC detail with valid, invalid and non existing ID returns expected result")
    @Order(0)
    void getJsonContentDetail() {
        //test exising ID
        var idToGet = jcIdList.get(0);
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/authenticated/json/{id}", idToGet)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(idToGet.intValue()))
                .body("name", notNullValue())
                .body("path", notNullValue())
                .body("json", notNullValue());

        //Test not existing ID
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/authenticated/json/{id}", 999)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));

        //Test invalid ID
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/authenticated/json/{id}", "notValidId")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));

    }

    @Test
    @DisplayName("Get JC list for current logged user returns list")
    @Order(1)
    void getJsonContentCurrentUserList() {
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/authenticated/json")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size", equalTo(15))
                .body("content", hasSize(3)) // Counting that the setUp method saves three objects
                .body("content[0].name", notNullValue())
                .body("content[0].path", notNullValue());

    }

    @Test
    @DisplayName("Create JC persists JC in DB and returns it when queried by ID")
    @Order(2)
    void createJsonContentDetail() {

        String newJsonContent = """
                {
                  "name": "New Json Content",
                  "json": "['key':'value']",
                  "path": "/json/4/new-json-content"
                }""";

        Long newId =
                given()
                        .log()
                        .ifValidationFails()
                        .header("authorization", "Bearer " + clientJwt)
                        .and().header("Accept-Language", "es")
                        .accept(ContentType.JSON)
                        .contentType(ContentType.JSON)
                        .body(newJsonContent)
                        .when()
                        .post("/api/v1/authenticated/json")
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract()
                        .as(Long.class);

        assertNotNull(newId);

        // Retrieve new JsonContent to confirm addition
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/authenticated/json/{id}", newId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("New Json Content"))
                .body("json", equalTo("['key':'value']"))
                .body("path", equalTo("/json/4/new-json-content"));

    }

    @Test
    @DisplayName("Patch JC modifies DB and returns modified version when queried")
    @Order(3)
    void updateJsonContentDetail() {

        Long idToUpdate = jcIdList.get(0); // Modify as necessary
        String updatedJsonContent = """
                {
                  "name": "Updated Json Content",
                  "json": "{'key': 'value'}",
                  "path": "/json/4/updated-json-content"
                }""";

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .contentType(ContentType.JSON)
                .body(updatedJsonContent)
                .when()
                .patch("/api/v1/authenticated/json/" + idToUpdate)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Retrieve to confirm update
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/authenticated/json/{id}", idToUpdate)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("Updated Json Content"))
                .body("json", equalTo("{'key': 'value'}"))
                .body("path", equalTo("/json/4/updated-json-content"));

    }

    @Test
    @DisplayName("Delete JC deletes it from DB and returns 404 when queried")
    @Order(4)
    void deleteJsonContentDetail() {

        Long idToDelete = jcIdList.get(0); // Update index to choose id to delete

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v1/authenticated/json/{id}", idToDelete)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Attempt to retrieve deleted JsonContent to confirm deletion
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + clientJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/authenticated/json/{id}", idToDelete)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

}