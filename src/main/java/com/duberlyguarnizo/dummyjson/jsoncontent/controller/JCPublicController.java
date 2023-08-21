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

package com.duberlyguarnizo.dummyjson.jsoncontent.controller;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContentService;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/public/json")
@Tag(name = "Public", description = "Public endpoints for visitors")
public class JCPublicController {
    JsonContentService service;
    private final ControllerUtils utils;

    public JCPublicController(JsonContentService service, ControllerUtils utils) {
        this.service = service;
        this.utils = utils;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonContentDetailDto> getJsonContentDetail(@PathVariable Long id) {
        var jsonDto = service.getById(id);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping //TODO: implement endpoint for top or recent public json
    public ResponseEntity<Page<JsonContentBasicDto>> getJsonContentDetailByName(@RequestParam(name = "name") String name,
                                                                                @PageableDefault(sort = {"id"}) Pageable page) {

        var jsonDto = service.getByName(URLDecoder.decode(name, StandardCharsets.UTF_8), page);
        return ResponseEntity.ok(jsonDto);
    }


}
