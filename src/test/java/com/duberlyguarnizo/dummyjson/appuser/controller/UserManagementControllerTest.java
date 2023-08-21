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

package com.duberlyguarnizo.dummyjson.appuser.controller;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementControllerTest {
    @Container // IntelliJ IDE false positive at PostgreSQLContainer<> about try-with-resources
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");
    private static final List<Long> idList = new ArrayList<>(); //container for id's of originally created managers
    static Faker faker = new Faker();
    private static String supervisorJwt;

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
        userRepository.deleteAll();
        AppUser supervisor = userRepository.save(AppUser.builder()
                .names("admin user")
                .email("adminemail@admin.com")
                .username("admin")
                .password(pwEncoder.encode("pass"))
                .idCard("12345678")
                .role(AppUserRole.SUPERVISOR)
                .active(true)
                .locked(false)
                .build()
        );
        supervisorJwt = jwtUtil.generateToken(supervisor);
        tokenService.saveToken(supervisorJwt, supervisor.getId());
        RestAssuredMockMvc.webAppContextSetup(context);

    }

    @Test
    @DisplayName("User creation returns HTTP Created with valid data")
    @Order(0)
    void createUsers() {
        for (int i = 0; i < 10; i++) {
            var name = faker.name();
            var tmpUser = AppUserRegistrationDto.builder()
                    .names(name.fullName())
                    .idCard(faker.expression("#{numerify '########'}"))
                    .email(faker.internet().emailAddress())
                    .username(name.username())
                    .password(faker.internet().password())
                    .role(AppUserRole.USER)
                    .build();

            var response = given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + supervisorJwt)
                    .and().header("Accept-Language", "es")
                    .body(tmpUser)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/v1/management/users")
                    .then()
                    .statusCode(201).extract().response();
            assertDoesNotThrow(() -> Long.parseLong(response.body().print()));
            idList.add(Long.parseLong(response.body().print()));
        }
    }

    @Test
    @DisplayName("User creation with invalid data returns error")
    @Order(1)
    void createUsersWithInvalidData() {

        var tmpUser = AppUserRegistrationDto.builder()
                .names("") //Required not empty String
                //.idCard("") required idCard not provided
                .email("invalid-email")
                .username(faker.name().username())
                .password(faker.internet().password())
                .role(AppUserRole.USER)
                .build();

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .body(tmpUser)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .body("") //empty body
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    @DisplayName("User creation with ADMIN or SUPERVISOR role returns error")
    @Order(2)
    void createUsersWithValidDataButInvalidRole() {

        var tempAdmin = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(faker.numerify("########"))
                .email(faker.internet().emailAddress())
                .username(faker.name().username())
                .password(faker.internet().password())
                .role(AppUserRole.ADMIN)
                .build();
        var tempSuper = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(faker.numerify("########"))
                .email(faker.internet().emailAddress())
                .username(faker.name().username())
                .password(faker.internet().password())
                .role(AppUserRole.SUPERVISOR)
                .build();

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .body(tempAdmin)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .body(tempSuper)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1/management/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    @DisplayName("User creation with existing username, email or idCard returns error")
    @Order(3)
    void createManagersOrUserWithValidDataButExistingUsernameOrIdCardOrEmail() {
        Long existingUserId = idList.get(4);
        var existingUser = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", existingUserId)
                .as(AppUserDetailDto.class);

        var tmpUser1 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(faker.numerify("########"))
                .email(faker.internet().emailAddress())
                .username(existingUser.getUsername())
                .password(faker.internet().password())
                .role(AppUserRole.USER)
                .build();
        var tmpUser2 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(existingUser.getIdCard())
                .email(faker.internet().emailAddress())
                .username(faker.name().username())
                .password(faker.internet().password())
                .role(AppUserRole.USER)
                .build();
        var tmpUser3 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(faker.numerify("########"))
                .email(existingUser.getEmail())
                .username(faker.name().username())
                .password(faker.internet().password())
                .role(AppUserRole.USER)
                .build();

        List<AppUserRegistrationDto> dtoList = List.of(tmpUser1, tmpUser2, tmpUser3);
        for (AppUserRegistrationDto dto : dtoList) {
            given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + supervisorJwt)
                    .and().header("Accept-Language", "es")
                    .body(dto)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/api/v1/management/users")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
        }

    }


    @Test
    @DisplayName("User GET with invalid id returns error")
    @Order(4)
    void getUserWithInvalidId() {
        Long userId = 987L; //Non-existing id
        String invalidId = "stuff"; //Non-valid id

        //return HTTP 404 Not Found on non-existing id
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));

        //Return HTTP 400 Bad Request on invalid id (like String instead of number)
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", invalidId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("User GET with valid id returns correct manager")
    @Order(5)
    void getUserById() {

        for (Long id : idList) {
            given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + supervisorJwt)
                    .and().header("Accept-Language", "es")
                    .get("/api/v1/management/users/{id}", id)
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(id.intValue()))
                    .body("username", not(emptyString()));
        }
    }


    @Test
    @DisplayName("User list returns all managers created plus original admin")
    @Order(6)
    void getAllUsers() {
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("numberOfElements", equalTo(10)) //10 users created in createUser() test method
                .body("empty", equalTo(false)) //empty json property of page indicates that there is content
                .body("content", hasSize(10));
    }

    @Test
    @DisplayName("User update does update the entity in the DB")
    @Order(7)
    void updateUser() {
        Long userId = idList.get(5);
        String newUserNames = faker.name().fullName();
        String newUserEmail = faker.internet().emailAddress();
        String newUserIdCard = faker.numerify("########");
        String newUserUsername = faker.name().username();


        //get name for manager
        var user = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", userId)
                .as(AppUserDetailDto.class);
        assertEquals(userId, user.getId());

        //change data for that manager
        AppUserRegistrationDto registration = AppUserRegistrationDto.builder()
                .names(newUserNames)
                .idCard(newUserIdCard)
                .email(newUserEmail)
                .username(newUserUsername)
                .role(AppUserRole.USER)
                .build();

        //Send PUT request and verify the changes... id must be the same
        given()
                .log()
                .all()
                .header("authorization", "Bearer " + supervisorJwt)
                .body(registration)
                .contentType(ContentType.JSON)
                .and().header("Accept-Language", "es")
                .patch("/api/v1/management/users/{id}", user.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        //now verify the changes:
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(userId.intValue())).and() //cast to int, as JsonPath returns int instead of long
                .body("names", equalTo(newUserNames)).and()
                .body("idCard", equalTo(newUserIdCard)).and()
                .body("email", equalTo(newUserEmail)).and()
                .body("username", equalTo(newUserUsername)).and()
                .body("role", equalTo(user.getRole().name())).and()
                .body("locked", equalTo(user.isLocked())).and()
                .body("active", equalTo(user.isActive())).and()
                .body("createdBy", equalTo(user.getCreatedBy().intValue())).and()
                .body("createdDate", equalTo(user.getCreatedDate().toString()));
    }

    @Test
    @DisplayName("User update returns error with invalid JSON body")
    @Order(8)
    void updateManagerWithInvalidData() {
        AppUserRegistrationDto registrationOne = AppUserRegistrationDto.builder()
                .names("") //empty name
                .idCard("") //empty idCard
                .email("invalid-email$#")
                .username("username")
                .role(AppUserRole.ADMIN) //invalid role for a user
                .build();


        given()
                .log()
                .all()
                .header("authorization", "Bearer " + supervisorJwt)
                .body(registrationOne)
                .contentType(ContentType.JSON)
                .and().header("Accept-Language", "es")
                .patch("/api/v1/management/users/{id}", 987)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("User update returns error if tried to change role to USER")
    @Order(9)
    void updateManagerWithValidDataButInvalidRole() {
        AppUserRegistrationDto registrationOne = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .username(faker.name().username())
                .idCard(faker.numerify("########"))
                .role(AppUserRole.SUPERVISOR) //invalid role for a manager
                .build();


        given()
                .log()
                .all()
                .header("authorization", "Bearer " + supervisorJwt)
                .body(registrationOne)
                .contentType(ContentType.JSON)
                .and().header("Accept-Language", "es")
                .patch("/api/v1/management/users/{id}", idList.get(5))
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("User update with existing username, idCard or Email returns error")
    @Order(10)
    void updateUserWithValidDataButExistingUsernameOrIdCardOrEmail() {
        Long firstUserId = idList.get(7);
        Long secondUserId = idList.get(6);
        var existingManager = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", firstUserId)
                .as(AppUserDetailDto.class);
        assertEquals(firstUserId, existingManager.getId());

        //Create a registrationDto with the username, idCard and email of the first manager and the id of the second manager
        AppUserRegistrationDto dto1 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .username(existingManager.getUsername())
                .idCard(faker.numerify("########"))
                .build();
        AppUserRegistrationDto dto2 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .idCard(existingManager.getIdCard())
                .build();
        AppUserRegistrationDto dto3 = AppUserRegistrationDto.builder()
                .names(faker.name().fullName())
                .email(existingManager.getEmail())
                .build();
        List<AppUserRegistrationDto> dtoList = List.of(dto1, dto2, dto3);

        for (AppUserRegistrationDto dto : dtoList) {
            given()
                    .log()
                    .ifValidationFails()
                    .header("authorization", "Bearer " + supervisorJwt)
                    .body(dto)
                    .contentType(ContentType.JSON)
                    .and().header("Accept-Language", "es")
                    .patch("/api/v1/management/users/{id}", secondUserId)
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
        }
    }


    @Test
    @DisplayName("User delete removes the entity from DB")
    @Order(11)
    void deleteUser() {
        Long userId = idList.get(5);
        var manager = given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", userId)
                .as(AppUserDetailDto.class);
        assertEquals(userId, manager.getId());

        //delete the manager
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        //Verify manager no longer exists
        given()
                .log()
                .all()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .get("/api/v1/management/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("User delete with invalid id returns error")
    @Order(12)
    void deleteNonExistingUser() {
        Long userId = 987L; //Non-existing id
        String invalidId = "stuff";

        //return HTTP 404 Not Found on non-existing id
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()));

        //Return HTTP 400 Bad Request on invalid id (like String instead of number)
        given()
                .log()
                .ifValidationFails()
                .header("authorization", "Bearer " + supervisorJwt)
                .and().header("Accept-Language", "es")
                .delete("/api/v1/management/users/{id}", invalidId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()));
    }

}