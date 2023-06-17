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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authenticated/json")
@SecurityRequirement(name = "Authorization Bearer")
@Tag(name = "Authenticated", description = "Endpoints for authenticated users, specifically for managing JSON content")
public class JsonContentAuthenticatedAPIController {
    JsonContentService service;

    public JsonContentAuthenticatedAPIController(JsonContentService service) {
        this.service = service;
    }


    @GetMapping("/{id}")
    public ResponseEntity<JsonContentDetailDto> getJsonContentDetail(@PathVariable Long id) {
        var jsonDto = service.getById(id);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping
    public ResponseEntity<Page<JsonContentBasicDto>> getJsonContentCurrentUserList(@RequestParam(required = false, defaultValue = "0") int page,
                                                                                   @RequestParam(required = false, defaultValue = "15") int size,
                                                                                   @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(ControllerUtils.processPageSort(sort)));
        var jsonDtoList = service.getAllByCurrentUser(pageRequest);
        return ResponseEntity.ok(jsonDtoList);
    }


    @PostMapping
    public ResponseEntity<Long> createJsonContentDetail(@Valid @RequestBody JsonContentCreationDto jsonDto) {
        Long id = service.create(jsonDto);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
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
