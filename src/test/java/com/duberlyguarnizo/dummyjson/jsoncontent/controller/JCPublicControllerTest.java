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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JCPublicControllerTest {
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    private static final Long loggedUserId = 3L;
    private static final List<Long> idList = new ArrayList<>(); //container for id's of originally created JsonContents
    static Faker faker = new Faker();
    private static String loggedUserJwt;
    @Autowired
    JsonContentRepository methodJcRepository;

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
        userRepository.deleteAll();


        //create an AppUser with role USER
        AppUser clientUser = userRepository.save(AppUser.builder()
                .id(loggedUserId)
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

        loggedUserJwt = jwtUtil.generateToken(clientUser);
        tokenService.saveToken(loggedUserJwt, clientUser.getId());
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
        jc2.setCreatedBy(2L);
        jc3.setCreatedBy(2L);
        idList.add(setUpJcRepository.save(jc1).getId());
        idList.add(setUpJcRepository.save(jc2).getId());
        idList.add(setUpJcRepository.save(jc3).getId());
    }

    @Test
    @DisplayName("Test get public JC detail by ID")
    @Order(0)
    void getJsonContentDetail_whenContentExists_returnsContent() {

        for (Long id : idList) {
            given()
                    .log()
                    .ifValidationFails()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/api/v1/public/json/{id}", id)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(id.intValue()))
                    .body("path", notNullValue())
                    .body("name", notNullValue())
                    .body("json", notNullValue());
        }
    }

    @Test
    @DisplayName("Test get public JC detail by ID as a logged user")
    @Order(1)
    void getJsonContentDetail_asLoggedUser_whenContentExists_returnsContent() {

        for (Long id : idList) {

            given()
                    .log()
                    .ifValidationFails()
                    .accept(ContentType.JSON)
                    .header("authorization", "Bearer " + loggedUserJwt)
                    .and().header("Accept-Language", "es")
                    .when()
                    .get("/api/v1/public/json/{id}", id)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(id.intValue()))
                    .body("path", notNullValue())
                    .body("name", notNullValue())
                    .body("json", notNullValue());
        }
    }

    @Test
    @DisplayName("Test get public JC detail by non existing ID")
    @Order(2)
    void getJsonContentDetail_whenContentDoesNotExist_returnsNotFound() {
        long id = 999L;
        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/" + id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    @DisplayName("Test get public JC detail by non existing ID as a logged user")
    @Order(3)
    void getJsonContentDetail_asLoggedUser_whenContentDoesNotExist_returnsNotFound() {
        long id = 999L;

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + loggedUserJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/" + id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Test get public JC detail by invalid ID")
    @Order(4)
    void getJsonContentDetail_whenIdIsInvalid_returnsBadRequest() {
        String id = "invalid-id";
        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/" + id)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Test get public JC detail by invalid ID as a logged user")
    @Order(5)
    void getJsonContentDetail_asLoggedUser_whenIdIsInvalid_returnsBadRequest() {
        String id = "invalid-id";

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + loggedUserJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/" + id)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }


    @Test
    @DisplayName("Test get public JC detail by valid name")
    @Order(6)
    void getJsonContentDetailByName_whenContentExists_returnsPageContent() {
        String existingName = "pharmacy product";

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/by-name/" + URLEncoder.encode(existingName, StandardCharsets.UTF_8))
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content", not(emptyArray()))
                .body("content[0].name", equalTo("pharmacy product"))
                .body("empty", equalTo(false));
    }

    @Test
    @DisplayName("Test get public JC detail by valid name as logged user")
    @Order(7)
    void getJsonContentDetailByName_asLoggedUser_whenContentExists_returnsPageContent() {
        String existingName = "pharmacy product";

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + loggedUserJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/by-name/" + URLEncoder.encode(existingName, StandardCharsets.UTF_8))
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("content", not(emptyArray()))
                .body("content[0].name", equalTo("pharmacy product"))
                .body("empty", equalTo(false));
    }

    @Test
    @DisplayName("Test get public JC detail by non existing name")
    @Order(8)
    void getJsonContentDetailByName_whenContentDoesNotExist_returnsEmptyPage() {
        String nonExistingName = "plea for innocence";

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/by-name/" + URLEncoder.encode(nonExistingName, StandardCharsets.UTF_8))
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("empty", equalTo(true));
    }

    @Test
    @DisplayName("Test get public JC detail by non existing name as logged user")
    @Order(9)
    void getJsonContentDetailByName_asLoggedUser_whenContentDoesNotExist_returnsEmptyPage() {
        String nonExistingName = "plea for innocence";
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + loggedUserJwt)
                .and().header("Accept-Language", "es")
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/json/by-name/" + URLEncoder.encode(nonExistingName, StandardCharsets.UTF_8))
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("empty", equalTo(true));
    }

}