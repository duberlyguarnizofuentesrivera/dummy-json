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
