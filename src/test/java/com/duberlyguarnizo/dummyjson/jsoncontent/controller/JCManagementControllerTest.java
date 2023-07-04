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
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JCManagementControllerTest {
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    private static final List<Long> idList = new ArrayList<>(); //container for id's of originally created managers
    static Faker faker = new Faker();
    static private String adminJwt;

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
                             @Autowired JsonContentRepository jcRepository,
                             @Autowired WebApplicationContext context,
                             @Autowired PasswordEncoder pwEncoder) {

        AppUser user = userRepository.save(AppUser.builder()
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
        String jwt = jwtUtil.generateToken(user);
        adminJwt = jwt;
        tokenService.saveToken(jwt, 1L);
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

        idList.add(jcRepository.save(jc1).getId());
        idList.add(jcRepository.save(jc2).getId());
        idList.add(jcRepository.save(jc3).getId());
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
                .statusCode(404)
                .body("status", equalTo(404));
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .when()
                .get("/api/v1/management/json/{id}", invalidFormatId)
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
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
                .statusCode(200)
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
                .statusCode(200)
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
                .statusCode(200)
                .body("id", equalTo(existingId1.intValue()))
                .assertThat().body(matchesJsonSchema(jsonSchema));


    }
}