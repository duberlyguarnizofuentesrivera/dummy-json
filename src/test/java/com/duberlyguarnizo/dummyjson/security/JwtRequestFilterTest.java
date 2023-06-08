package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.employee.Employee;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtRequestFilterTest {
    @Mock
    CustomUserDetailService userDetailService;

    @Mock
    JwtUtil jwtUtil;
    private JwtRequestFilter filter;

    @Test
    void shouldNotProcessWithInvalidExistingAuthHeader() throws ServletException, IOException {
        filter = new JwtRequestFilter(userDetailService, jwtUtil);
        String authorizationHeader = "invalid_auth_ header";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured_endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("authorization", authorizationHeader);
        filter.doFilterInternal(request, response, mock(FilterChain.class));

        verify(jwtUtil, times(0)).extractUsername(anyString());
        verify(jwtUtil, times(0)).validateToken(anyString(), any(Employee.class));
    }

    @Test
    void shouldNotProcessWithExistingValidAuthHeaderAndNonExistingUser() throws ServletException, IOException {
        filter = new JwtRequestFilter(userDetailService, jwtUtil);
        //example token with user "peterg" (for Peter Griffin, from Family Guy)
        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwZXRlcmciLCJpYXQiOjE2ODQ3NzQxODEsImV4cCI6MTY4NDgxMDE4MX0.JZyoRiQ553ujDFlaUWL5HJPW8Ev2OZXNdT2XzW5lDXw";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured_endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("authorization", authorizationHeader);

        filter.doFilterInternal(request, response, mock(FilterChain.class));
        verify(jwtUtil, times(1)).extractUsername(anyString());
        verify(jwtUtil, times(0)).validateToken(anyString(), any(Employee.class));
    }
}