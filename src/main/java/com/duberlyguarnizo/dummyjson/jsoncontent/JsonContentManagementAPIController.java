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

package com.duberlyguarnizo.dummyjson.jsoncontent;

import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentCreationDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/management/json")
@SecurityRequirement(name = "Authorization Bearer")
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
@Tag(name = "Management", description = "Endpoints for managers to administer JSON content created by users")
public class JsonContentManagementAPIController {
    JsonContentService service;
    private final ControllerUtils utils;

    public JsonContentManagementAPIController(JsonContentService service, ControllerUtils utils) {
        this.service = service;
        this.utils = utils;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonContentDetailDto> getJsonContentDetail(@PathVariable Long id) {
        var jsonDto = service.getById(id);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping("/by-user/{id}")
    public ResponseEntity<Page<JsonContentBasicDto>> getJsonContentDetailByIdForUser(@PathVariable Long id,
                                                                                     @RequestParam(required = false, defaultValue = "0") int page,
                                                                                     @RequestParam(required = false, defaultValue = "15") int size,
                                                                                     @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(utils.processPageSort(sort)));
        var jsonDto = service.getAllByUserId(id, pageRequest);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping()
    public ResponseEntity<Page<JsonContentBasicDto>> getJsonContentAllList(@RequestParam(required = false, defaultValue = "0") int page,
                                                                           @RequestParam(required = false, defaultValue = "15") int size,
                                                                           @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(utils.processPageSort(sort)));
        var jsonDtoList = service.getAllByAnyUser(pageRequest);
        return ResponseEntity.ok(jsonDtoList);
    }

    @PutMapping
    public ResponseEntity<Void> updateJsonContentDetail(@Valid @RequestBody JsonContentCreationDto jsonDto) {
        service.updateOwnJsonContent(jsonDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteJsonContentDetail(@PathVariable Long id) {
        service.deleteOwnJsonContent(id);
        return ResponseEntity.noContent().build();
    }
}
