package com.duberlyguarnizo.dummyjson.appuser;

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserBasicDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserMapper;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import com.duberlyguarnizo.dummyjson.auditing.CustomAuditorAware;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.NotOwnedObjectException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper mapper;
    private final CustomAuditorAware auditorAware;

    public AppUserDetailDto getEmployeeById(Long id) {
        return mapper.toDetailDto(appUserRepository.findById(id).orElseThrow(() -> new IdNotFoundException("No appuser found with id: " + id + " in the database")));
    }

    public List<AppUserBasicDto> getAllEmployees() {
        return appUserRepository.findAll().stream().map(mapper::toBasicDto).toList();
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can create users
    public Long createEmployee(@Valid AppUserRegistrationDto registrationDto) throws RepositoryException {
        registrationDto.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        AppUser convertedAppUser = mapper.toEntity(registrationDto);
        convertedAppUser.setActive(true);
        try {
            return appUserRepository.save(convertedAppUser).getId();
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Unable to create user, data is invalid.");
        } catch (OptimisticLockingFailureException e) {
            throw new RepositoryException("Optimistic locking error, please try again");
        }

    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can delete users
    public void deleteEmployee(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException("You do not have the permissions to delete this resource.");
        } else {
            var employee = appUserRepository.findById(id).orElseThrow(() -> new IdNotFoundException("No user found with id: " + id + " in the database."));
            Long currentUserId = currentAuditor.get();
            if (employee.getId().equals(currentUserId)) {
                throw new NotOwnedObjectException("You cannot  delete your own user!");
            }
            appUserRepository.deleteById(id);
        }
    }
}
