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
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserBasicDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/management/users")
@SecurityRequirement(name = "Authorization Bearer") //swagger UI
@Tag(name = "Managers", description = "Endpoints of CRUD methods  for managers to manage users") //Swagger UI
@RequiredArgsConstructor
public class UserManagementController {
    private final AppUserService appUserService;
    private final ControllerUtils utils;

    @GetMapping()
    public ResponseEntity<Page<AppUserBasicDto>> getAllUsers(@RequestParam(required = false, defaultValue = "0") int page,
                                                             @RequestParam(required = false, defaultValue = "15") int size,
                                                             @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(utils.processPageSort(sort))); //sort is validated in utility method
        var list = appUserService.getAllUsers(pageRequest);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDetailDto> getUser(@PathVariable Long id) {
        var result = appUserService.getAppUserById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> createUser(@Valid @RequestBody AppUserRegistrationDto registrationDto) {
        var result = appUserService.createUser(registrationDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @Valid @RequestBody AppUserRegistrationDto registrationDto) {
        appUserService.partialUpdateUser(id, registrationDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivates a user with the given id. ADMIN or SUPERVISOR can deactivate a user.
     *
     * @param id the id of the user to deactivate.
     * @return a ResponseEntity with a void body. The response status will be 200 (Ok) on success.
     */
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        appUserService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
}
