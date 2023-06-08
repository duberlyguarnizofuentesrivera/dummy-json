package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var possibleEmployee = employeeRepository.findByUsernameIgnoreCase(username);
        if (possibleEmployee.isEmpty()) {
            throw new UsernameNotFoundException("The user with the name " + username + " could not be found!");
        } else {
            return possibleEmployee.get();
        }
    }
}
