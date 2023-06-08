package com.duberlyguarnizo.dummyjson.employee;

import com.duberlyguarnizo.dummyjson.employee.dto.EmployeeRegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
public class EmployeeApiController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Long> createEmployee(@RequestBody EmployeeRegistrationDto registrationDto) throws RuntimeException {
        var result = employeeService.createEmployee(registrationDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
