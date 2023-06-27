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

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
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
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    static Faker faker = new Faker();
    static private String adminJwt;
    private static final List<Long> idList = new ArrayList<>(); //container for id's of originally created managers

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

    }

    @Test
    @DisplayName("Manager login with incorrect credentials return error")
    @Order(0)
    void testAdminLoginInvalid() {
        String postLoginBody = "{ \"username\": \"notAdmin\", \"password\": \"invalidPass\"}";
        given()
                .header("Accept-Language", "es")
                .body(postLoginBody)
                .contentType(ContentType.JSON)
                .log()
                .everything()
                .post("/api/v1/auth/authenticate")
                .then()
                .statusCode(401)
                .body("status", equalTo(401));
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
            assertDoesNotThrow(() -> Long.parseLong(response.body().print()));
            idList.add(Long.parseLong(response.body().print()));
        }
    }

    @Test
    @DisplayName("Manager creation with invalid data returns error")
    @Order(3)
    void createManagersWithInvalidData() {

        var name = faker.name();
        var tmpUser = AppUserRegistrationDto.builder()
                .names("") //Required not empty String
                //.idCard("") required idCard not provided
                .email("invalid-email")
                .username(name.username())
                .password(faker.internet().password())
                .role(AppUserRole.SUPERVISOR)
                .build();

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .body(tmpUser)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/managers")
                .then()
                .statusCode(400);

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .body("") //empty body
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/managers")
                .then()
                .statusCode(400);

    }


    @Test
    @DisplayName("Manager GET with invalid id returns error")
    @Order(4)
    void getManagerWithInvalidId() {
        Long managerId = 987L; //Non-existing id
        String invalidId = "stuff";

        //return HTTP 404 Not Found on non-existing id
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", managerId)
                .then()
                .statusCode(404)
                .body("status", equalTo(404));

        //Return HTTP 400 Bad Request on invalid id (like String instead of number)
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", invalidId)
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
    }

    @Test
    @DisplayName("Manager GET with valid id returns correct manager")
    @Order(5)
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
                    .body("id", equalTo(id.intValue()))
                    .body("username", not(emptyString()));
        }
    }

    //TODO: get manager with non existing ID and NAN id

    @Test
    @DisplayName("Manager list returns all managers created plus original admin")
    @Order(6)
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

    @Test
    @DisplayName("Manager update does update the entity in the DB")
    @Order(7)
    void updateManager() {
        Long managerId = idList.get(5);
        String newManagerNames = faker.name().fullName();
        String newManagerEmail = faker.internet().emailAddress();
        String newManagerIdCard = faker.numerify("########");
        String newManagerUsername = faker.name().username();


        //get name for manager
        var manager = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", managerId)
                .as(AppUserDetailDto.class);
        assertEquals(managerId, manager.getId());

        //change data for that manager
        AppUserRegistrationDto registration = AppUserRegistrationDto.builder()
                .id(manager.getId()) //PUT requires to provide ID field
                .names(newManagerNames)
                .idCard(newManagerIdCard)
                .email(newManagerEmail)
                .username(newManagerUsername)
                .role(manager.getRole() == AppUserRole.ADMIN ? AppUserRole.SUPERVISOR : AppUserRole.ADMIN)
                .build();

        //Send PUT request and verify the changes... id must be the same
        given()
                .log()
                .all()
                .header("authorization", "Bearer " + adminJwt)
                .body(registration)
                .contentType(ContentType.JSON)
                .and().header("Accept-Language", "es")
                .put("/api/v1/management/managers")
                .then()
                .statusCode(204);

        //now verify the changes:
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", managerId)
                .then()
                .statusCode(200)
                .body("id", equalTo(managerId.intValue())).and() //cast to int, as JsonPath returns int instead of long
                .body("names", equalTo(newManagerNames)).and()
                .body("idCard", equalTo(newManagerIdCard)).and()
                .body("email", equalTo(newManagerEmail)).and()
                .body("username", equalTo(newManagerUsername));
    }

    //TODO: update manager with invalid body

    @Test
    @DisplayName("Manager delete removes the entity from DB")
    @Order(8)
    void deleteManager() {
        Long managerId = idList.get(5);
        var manager = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", managerId)
                .as(AppUserDetailDto.class);
        assertEquals(managerId, manager.getId());

        //delete the manager
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/managers/{id}", managerId)
                .then()
                .statusCode(204);

        //Verify manager no longer exists
        given()
                .log()
                .all()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/managers/{id}", managerId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Manager delete with invalid id returns error")
    @Order(9)
    void deleteNonExistingManager() {
        Long managerId = 987L; //Non-existing id
        String invalidId = "stuff";

        //return HTTP 404 Not Found on non-existing id
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/managers/{id}", managerId)
                .then()
                .statusCode(404)
                .body("status", equalTo(404));

        //Return HTTP 400 Bad Request on invalid id (like String instead of number)
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + adminJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/managers/{id}", invalidId)
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
    }

}