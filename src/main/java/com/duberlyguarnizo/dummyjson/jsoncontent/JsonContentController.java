package com.duberlyguarnizo.dummyjson.jsoncontent;

import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentCreationDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/api/v1/json-content")
public class JsonContentController {
    JsonContentService service;

    public JsonContentController(JsonContentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonContentDetailDto> getJsonContentDetail(@PathVariable Long id) {
        var jsonDto = service.getById(id);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping
    public ResponseEntity<List<JsonContentBasicDto>> getJsonContentDetail() {
        var jsonDtoList = service.getAllByCurrentUser();
        if (jsonDtoList.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(jsonDtoList);
    }

    @PostMapping
    public ResponseEntity<Long> createJsonContentDetail(@Valid @RequestBody JsonContentCreationDto jsonDto) {
        Long id = service.create(jsonDto);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> updateJsonContentDetail(@Valid @RequestBody JsonContentCreationDto jsonDto) {
        service.update(jsonDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteJsonContentDetail(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
