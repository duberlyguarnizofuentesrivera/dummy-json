package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class JwtRequestFilterTest {
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
        //example jwt_token with user "peterg" (for Peter Griffin, from Family Guy)
        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwZXRlcmciLCJpYXQiOjE2ODQ3NzQxODEsImV4cCI6MTY4NDgxMDE4MX0.JZyoRiQ553ujDFlaUWL5HJPW8Ev2OZXNdT2XzW5lDXw";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured_endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("authorization", authorizationHeader);

        filter.doFilterInternal(request, response, mock(FilterChain.class));
        verify(jwtUtil, times(1)).extractUsername(anyString());
        verify(jwtUtil, times(0)).validateToken(anyString(), any(AppUser.class));
    }
}