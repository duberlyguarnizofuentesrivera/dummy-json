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

import com.duberlyguarnizo.dummyjson.appuser.AppUserService;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authenticated/user")
@SecurityRequirement(name = "Authorization Bearer") //swagger UI
@Tag(name = "Users", description = "Endpoints of CRUD methods  for users") //Swagger UI
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService service;

    @GetMapping("/profile")
    public ResponseEntity<AppUserDetailDto> getUserProfile() {
        var currentUser = service.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PatchMapping
    public ResponseEntity<Long> editUserProfile(@RequestBody AppUserRegistrationDto dto) {
        var currentUser = service.getCurrentUser();
        service.partialUpdateOwnUser(dto);
        return new ResponseEntity<>(currentUser.getId(), HttpStatus.NO_CONTENT);
    }
}
