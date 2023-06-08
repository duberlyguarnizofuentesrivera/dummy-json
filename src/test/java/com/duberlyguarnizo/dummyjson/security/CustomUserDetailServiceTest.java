package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.employee.Employee;
import com.duberlyguarnizo.dummyjson.employee.EmployeeRepository;
import com.duberlyguarnizo.dummyjson.employee.EmployeeRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CustomUserDetailServiceTest {
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder pwdEncoder;

    @BeforeEach
    void setUp() {
        Employee user = Employee.builder()
                .names("Jhon Doe")
                .idCard("987654321")
                .email("doe@mail.com")
                .isActive(true)
                .username("jhondoe")
                .password(pwdEncoder.encode("doejhon"))
                .role(EmployeeRole.SUPERVISOR)
                .build();
        employeeRepository.save(user);

    }

    @AfterEach
    void destroy() {
        employeeRepository.deleteAll();
    }


    @Test
    @DisplayName("UserDetailService returns correct Employee with a username that does exist")
    void testLoadUserByUsername() throws UsernameNotFoundException {
        // Arrange
        String username = "jhondoe";
        // Act
        Employee employee = (Employee) customUserDetailService.loadUserByUsername(username);

        // Assert
        assertEquals(username, employee.getUsername());
    }

    @Test
    @DisplayName("UserDetailService throws exception when loading a username that does not exist")
    void testLoadUserByUsername_NotFound() throws UsernameNotFoundException {
        // Arrange
        String username = "not_jhondoe";

        // Should throw UsernameNotFoundException
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailService.loadUserByUsername(username));
    }
}