package com.duberlyguarnizo.dummyjson.employee;

import com.duberlyguarnizo.dummyjson.employee.dto.EmployeeDtoMapper;
import com.duberlyguarnizo.dummyjson.employee.dto.EmployeeRegistrationDto;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Long createEmployee(@Valid EmployeeRegistrationDto registrationDto) throws RepositoryException {
        registrationDto.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        Employee convertedEmployee = EmployeeDtoMapper.INSTANCE.employeeRegistrationDtoToEmployee(registrationDto);
        convertedEmployee.setActive(true);
        try {

            return employeeRepository.save(convertedEmployee).getId();
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Unable to create employee, invalid data.");
        } catch (OptimisticLockingFailureException e) {
            throw new RepositoryException("Optimistic locking error, please try again");
        }

    }

}
