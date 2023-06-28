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

package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import com.duberlyguarnizo.dummyjson.appuser.ManagerManagementController;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class JwtRequestFilterTest {
    private static String adminJwt;
    @Container
    public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            "postgres:latest")
            .withUsername("tc_user")
            .withPassword("tc_password")
            .withDatabaseName("tc_db");

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
                             @Autowired ManagerManagementController userApiController,
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


    @Mock
    JwtUtil jwtUtil;
    @Autowired
    private JwtRequestFilter filter;

    @Test
    void shouldNotProcessWithInvalidExistingAuthHeader() throws ServletException, IOException {
        String authorizationHeader = "invalid_auth_ header";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured_endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("authorization", authorizationHeader);
        filter.doFilterInternal(request, response, mock(FilterChain.class));

        verify(jwtUtil, times(0)).extractUsername(anyString());
        verify(jwtUtil, times(0)).validateToken(anyString(), any(AppUser.class));
    }

    @Test
    void shouldNotProcessWithExistingValidAuthHeaderAndNonExistingUser() throws ServletException, IOException {
        //example jwt_token with user "peterg" (for Peter Griffin, from Family Guy)... token should be invalid
        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwZXRlcmciLCJpYXQiOjE2ODQ3NzQxODEsImV4cCI6MTY4NDgxMDE4MX0.JZyoRiQ553ujDFlaUWL5HJPW8Ev2OZXNdT2XzW5lDXw";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured_endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("authorization", authorizationHeader);

        assertThrows(ExpiredJwtException.class, () -> filter.doFilterInternal(request, response, mock(FilterChain.class)));
        //since no processing takes place, JWT Util functions should not be called
        verify(jwtUtil, times(0)).extractUsername(anyString());
        verify(jwtUtil, times(0)).validateToken(anyString(), any(AppUser.class));
    }
}