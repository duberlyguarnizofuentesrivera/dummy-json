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

package com.duberlyguarnizo.dummyjson.appuser;

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenService;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import io.restassured.http.ContentType;
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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppUserApiControllerTest {
    @Container
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    static Faker faker = new Faker();
    static private String adminJwt;
    private final List<Long> idList = new ArrayList<>(); //container for id's of originally created managers

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
                             @Autowired AppUserApiController userApiController,
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
                .isActive(true)
                .isLocked(false)
                .build()
        );
        String jwt = jwtUtil.generateToken(user);
        adminJwt = jwt;
        tokenService.saveToken(jwt, 1L);
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Test
    @DisplayName("Manager login with correct credentials return valid JWT")
    @Order(1)
    void testAdminLogin() {
        String postLoginBody = "{ \"username\": \"admin\", \"password\": \"pass\"}";
        String jwt =
                given()
                        .header("Accept-Language", "es")
                        .body(postLoginBody)
                        .contentType(ContentType.JSON)
                        .log()
                        .everything()
                        .post("/api/v1/auth/authenticate")
                        .jsonPath()
                        .get("jwt");
        assertNotNull(jwt);
        assertFalse(jwt.isEmpty());
        adminJwt = jwt;
    }


    @Test
    @DisplayName("Manager creation returns HTTP Created with valid data")
    @Order(2)
    void createManagers() {
        for (int i = 0; i < 10; i++) {
            var name = faker.name();
            var tmpUser = AppUserRegistrationDto.builder()
                    .names(name.fullName())
                    .idCard(faker.expression("#{numerify '########'}"))
                    .email(faker.internet().emailAddress())
                    .username(name.username())
                    .password(faker.internet().password())
                    .role(i % 2 == 0 ? AppUserRole.SUPERVISOR : AppUserRole.ADMIN)
                    .build();
            var response = given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + adminJwt)
                    .and().header("Accept-Language", "es")
                    .body(tmpUser)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/v1/management/managers")
                    .then()
                    .statusCode(201).extract().response();
            assertDoesNotThrow(() -> idList.add(Long.parseLong(response.body().print())));
        }
    }

    @Test
    @DisplayName("Manager GET with valid id returns correct manager")
    @Order(3)
    void getManagerById() {
        for (Long id : idList) {
            given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + adminJwt)
                    .and().header("Accept-Language", "es")
                    .get("/api/v1/management/managers/{id}", id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("username", not(emptyString()));
        }
    }

    @Test
    @Order(4)
    void getAllManagers() {
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers")
                .then()
                .statusCode(200)
                .body("numberOfElements", equalTo(11)) //remember original admin? That's why 10+1
                .body("content", hasSize(11))
                .body("empty", equalTo(false));
    }
//
//    @Test
//    void updateManager() {
//    }
//
//    @Test
//    void deleteManager() {
//    }
}