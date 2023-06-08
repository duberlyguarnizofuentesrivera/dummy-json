package com.duberlyguarnizo.dummyjson.employee;

import com.duberlyguarnizo.dummyjson.employee.dto.EmployeeDtoMapper;
import com.duberlyguarnizo.dummyjson.employee.dto.EmployeeRegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Long createEmployee(@Valid EmployeeRegistrationDto registrationDto) throws RuntimeException {
        registrationDto.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        Employee convertedEmployee = EmployeeDtoMapper.INSTANCE.employeeRegistrationDtoToEmployee(registrationDto);
        convertedEmployee.setActive(true);
        return employeeRepository.save(convertedEmployee).getId();

    }

}
