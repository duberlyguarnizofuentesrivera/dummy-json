package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class CustomUserDetailServiceTest {
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

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder pwdEncoder;

    @BeforeEach
    void setUp() {
        AppUser user = AppUser.builder()
                .names("Jhon Doe")
                .idCard("987654321")
                .email("doe@mail.com")
                .isActive(true)
                .username("jhondoe")
                .password(pwdEncoder.encode("doejhon"))
                .role(AppUserRole.SUPERVISOR)
                .build();
        appUserRepository.save(user);

    }

    @AfterEach
    void destroy() {
        appUserRepository.deleteAll();
    }


    @Test
    @DisplayName("UserDetailService returns correct appuser with a username that does exist")
    void testLoadUserByUsername() throws UsernameNotFoundException {
        // Arrange
        String username = "jhondoe";
        // Act
        AppUser appUser = (AppUser) customUserDetailService.loadUserByUsername(username);

        // Assert
        assertEquals(username, appUser.getUsername());
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