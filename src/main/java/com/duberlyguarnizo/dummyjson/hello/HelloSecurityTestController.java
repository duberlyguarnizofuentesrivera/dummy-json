package com.duberlyguarnizo.dummyjson.hello;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class HelloSecurityTestController {
    @GetMapping("hello-auth")
    public ResponseEntity<?> sayHelloAuth() {
        return ResponseEntity.ok("Hello! I'm just a secured endpoint");
    }

    @GetMapping("hello-super")
    public ResponseEntity<?> sayHelloSupervisor() {
        return ResponseEntity.ok("Hello! I'm a secured endpoint, and you are SUPERVISOR");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("hello-admin")
    public ResponseEntity<?> sayHelloAdmin() {
        return ResponseEntity.ok("Hello! I'm a secured endpoint, and you are ADMIN");
    }

    @GetMapping("public")
    public ResponseEntity<?> sayHelloPublic() {
        return ResponseEntity.ok("Hello! I'm a public endpoint");
    }
}
