package com.duberlyguarnizo.dummyjson.appuser;

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserBasicDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class AppUserApiController {
    private final AppUserService appUserService;

    public AppUserApiController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public ResponseEntity<List<AppUserBasicDto>> getEmployees() {
        var list = appUserService.getAllEmployees();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> createEmployee(@RequestBody AppUserRegistrationDto registrationDto) throws RuntimeException {
        var result = appUserService.createEmployee(registrationDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(Long id) {
        appUserService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
