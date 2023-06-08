package com.duberlyguarnizo.dummyjson.employee.dto;

import com.duberlyguarnizo.dummyjson.employee.Employee;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeDtoMapper {
    EmployeeDtoMapper INSTANCE = Mappers.getMapper(EmployeeDtoMapper.class);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Employee employeeRegistrationDtoToEmployee(EmployeeRegistrationDto registrationDto);
}
