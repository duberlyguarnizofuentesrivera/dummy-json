package com.duberlyguarnizo.dummyjson.appuser;

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

@RestController
@RequestMapping("/api/v1/management/managers")
@SecurityRequirement(name = "Authorization Bearer")
@Tag(name = "Managers", description = "Endpoints of CRUD methods  for managers")
public class AppUserApiController {
    private final AppUserService appUserService;
    private final ControllerUtils utils;

    public AppUserApiController(AppUserService appUserService, ControllerUtils utils) {
        this.appUserService = appUserService;
        this.utils = utils;
    }

    public ResponseEntity<Page<AppUserBasicDto>> getManagers(@RequestParam(required = false, defaultValue = "0") int page,
                                                             @RequestParam(required = false, defaultValue = "15") int size,
                                                             @RequestParam(required = false, defaultValue = "id,desc") String[] sort) {
        PageRequest pageRequest = PageRequest.of(page,
                size,
                Sort.by(utils.processPageSort(sort)));
        var list = appUserService.getAllManagers(pageRequest);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDetailDto> getManager(@PathVariable Long id) {
        var result = appUserService.getManagerById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> createManager(@Valid @RequestBody AppUserRegistrationDto registrationDto) {
        var result = appUserService.createManager(registrationDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Void> updateManager(@Valid @RequestBody AppUserRegistrationDto registrationDto) {
        appUserService.updateManager(registrationDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        appUserService.deleteManager(id);
        return ResponseEntity.noContent().build();
    }
}
