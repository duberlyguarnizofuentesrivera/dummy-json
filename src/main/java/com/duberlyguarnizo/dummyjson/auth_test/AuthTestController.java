package com.duberlyguarnizo.dummyjson.auth_test;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/test")
@Tag(name = "Authentication Test", description = "Role-restricted endpoints for testing authentication")
public class AuthTestController {
    @PreAuthorize("isAuthenticated()")
    @GetMapping("secured-endpoint")
    public ResponseEntity<String> sayHelloAuth() {
        return ResponseEntity.ok("Hello! I'm just a secured endpoint, and you are logged in.");
    }

    @PreAuthorize("hasAuthority('SUPERVISOR')")
    @GetMapping("supervisor-endpoint")
    public ResponseEntity<String> sayHelloSupervisor() {
        return ResponseEntity.ok("Hello! I'm a secured endpoint, and you are SUPERVISOR.");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("admin-endpoint")
    public ResponseEntity<String> sayHelloAdmin() {
        return ResponseEntity.ok("Hello! I'm a secured endpoint, and you are ADMIN.");
    }

    @GetMapping("public-endpoint")
    public ResponseEntity<String> sayHelloPublic() {
        return ResponseEntity.ok("Hello! I'm a public endpoint. You may or may not be logged in.");
    }
}
