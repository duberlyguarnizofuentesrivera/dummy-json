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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints of CRUD method for managers. Permissions for access are defined at service level.
 * All errors or exceptions are managed at service level, returning a ProblemDetail in such event.
 * An AppUser with the role ADMIN or SUPERVISOR is considered a "manager". All other roles are considered "users".
 */
@RestController
@RequestMapping("/api/v1/management/managers")
@SecurityRequirement(name = "Authorization Bearer") //swagger UI
@Tag(name = "Managers", description = "Endpoints of CRUD methods  for managers to manage other managers") //Swagger UI
public class ManagerManagementController {
    private final AppUserService appUserService;
    private final ControllerUtils utils;

    public ManagerManagementController(AppUserService appUserService, ControllerUtils utils) {
        this.appUserService = appUserService;
        this.utils = utils;
    }

    /**
     * Retrieves a page of basic details of all managers, sorted as specified.
     *
     * @param page the page number (0-indexed) of the page to retrieve. Defaults to 0.
     * @param size the maximum number of items to return on the requested page. Defaults to 15.
     * @param sort an array of sort directives in the format "property, direction". Defaults to sorting by ID in descending order. Sort is validated by utility service.
     * @return a ResponseEntity with a Page object containing basic details of the managers on the requested page, sorted as specified.
     * The response status will be 200 (OK) on success.
     */
    @GetMapping()
    public ResponseEntity<Page<AppUserBasicDto>> getAllManagers(@RequestParam(required = false, defaultValue = "0") int page,
                                                                @RequestParam(required = false, defaultValue = "15") int size,
                                                                @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(utils.processPageSort(sort))); //sort is validated in utility method
        var list = appUserService.getAllManagers(pageRequest);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    /**
     * Retrieves the details of the manager with the specified ID.
     *
     * @param id the ID of the manager to retrieve.
     * @return a ResponseEntity with the details of the manager. The response status will be 200 (OK) on success.
     * If no manager is found with the specified ID, the response status will be ProblemDetail with status 404 (Not Found).
     * If the ID provided is not a valid Long value, it will return a ProblemDetail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppUserDetailDto> getManager(@PathVariable Long id) {
        var result = appUserService.getManagerById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Creates a new manager based on the provided registration DTO. Only an ADMIN role can create a new manager.
     *
     * @param registrationDto the registration DTO containing the necessary details of the manager to be created. Validation is made at service level.
     * @return a ResponseEntity with the id of the newly created manager. The response status will be 201 (Created) on success. It will return a ProblemDetail (managed by the service).
     */
    @PostMapping
    public ResponseEntity<Long> createManager(@Valid @RequestBody AppUserRegistrationDto registrationDto) {
        var result = appUserService.createManager(registrationDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Updates an existing manager with the details provided in the given registration DTO. The <b>ID field must be provided</b> inside the request body
     *
     * @param registrationDto the registration DTO containing the updated details of the manager. Must include ID field.
     * @return a ResponseEntity with no content (204 No Content) on successful update.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateManager(@PathVariable Long id, @Valid @RequestBody AppUserRegistrationDto registrationDto) {
        appUserService.partialUpdateManager(id, registrationDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the manager corresponding to the provided id from the system.
     *
     * @param id the id of the manager to be deleted.
     * @return a ResponseEntity with no content (204 No Content) on successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        appUserService.deleteManager(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Deactivates a manager with the given id. Only role ADMIN can deactivate a manager.
     *
     * @param id the id of the manager to deactivate.
     * @return a ResponseEntity with a void body. The response status will be 200 (Ok) on success.
     */
    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateManager(@PathVariable Long id) {
        appUserService.deactivateManager(id);
        return ResponseEntity.ok().build();
    }
}
