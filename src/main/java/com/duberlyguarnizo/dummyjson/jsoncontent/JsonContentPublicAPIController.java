package com.duberlyguarnizo.dummyjson.jsoncontent;

import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentBasicDto;
import com.duberlyguarnizo.dummyjson.jsoncontent.dto.JsonContentDetailDto;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/json")
@Tag(name = "Public", description = "Public endpoints for visitors")
public class JsonContentPublicAPIController {
    JsonContentService service;

    public JsonContentPublicAPIController(JsonContentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonContentDetailDto> getJsonContentDetail(@PathVariable Long id) {
        var jsonDto = service.getById(id);
        return ResponseEntity.ok(jsonDto);
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<Page<JsonContentBasicDto>> getJsonContentDetailByName(@PathVariable String name,
                                                                                @RequestParam(required = false, defaultValue = "0") int page,
                                                                                @RequestParam(required = false, defaultValue = "15") int size,
                                                                                @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(ControllerUtils.processPageSort(sort)));
        var jsonDto = service.getByName(name, pageRequest);
        return ResponseEntity.ok(jsonDto);
    }


}
