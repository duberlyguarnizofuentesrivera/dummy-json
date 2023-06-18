package com.duberlyguarnizo.dummyjson.appuser;

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserBasicDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserMapper;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import com.duberlyguarnizo.dummyjson.auditing.CustomAuditorAware;
import com.duberlyguarnizo.dummyjson.exceptions.ForbiddenActionException;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper mapper;
    private final CustomAuditorAware auditorAware;
    private final ControllerUtils utils;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public AppUserDetailDto getManagerById(Long id) {
        return mapper.toDetailDto(
                appUserRepository
                        .findById(id)
                        .orElseThrow(() -> new IdNotFoundException(
                                utils.getMessage(
                                        "exception_id_not_found_manager_detail",
                                        new Long[]{id}))));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<AppUserBasicDto> getAllManagers(Pageable page) {
        return appUserRepository.findAll(page).map(mapper::toBasicDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can create users
    public Long createManager(@Valid AppUserRegistrationDto registrationDto) throws RepositoryException {
        registrationDto.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        AppUser convertedAppUser = mapper.toEntity(registrationDto);
        //TODO: verify that the username is unique
        convertedAppUser.setActive(true);
        convertedAppUser.setLocked(false);
        try {
            return appUserRepository.save(convertedAppUser).getId();
        } catch (IllegalArgumentException e) {
            throw new RepositoryException(utils.getMessage("exception_repository_save_error_invalid_user"));
        } catch (OptimisticLockingFailureException e) {
            throw new RepositoryException(utils.getMessage("exception_repository_save_error_optimistic_lock"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can edit users
    public void updateManager(AppUserRegistrationDto registrationDto) {
        var manager = appUserRepository.findById(
                        registrationDto.getId())
                .orElseThrow(
                        () -> new IdNotFoundException(
                                utils.getMessage(
                                        "exception_id_not_found_manager_detail",
                                        new Long[]{registrationDto.getId()})));
        var updatedManager = mapper.partialUpdate(registrationDto, manager);
        //TODO: verify change in username (must be unique)
        appUserRepository.save(updatedManager);
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can delete users
    public void deleteManager(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException(utils.getMessage("error_auditor_empty"));
        } else {
            var appUser = appUserRepository
                    .findById(id)
                    .orElseThrow(() -> new IdNotFoundException(
                            utils.getMessage(
                                    "exception_id_not_found_user_detail",
                                    new Long[]{id})));
            Long currentUserId = currentAuditor.get();
            if (appUser.getId().equals(currentUserId)) {
                throw new ForbiddenActionException(utils.getMessage("error_delete_own_user"));
            }
            appUserRepository.deleteById(id);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can deactivate other admins
    public void deactivateManager(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        var appUser = appUserRepository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException(
                        utils.getMessage(
                                "exception_id_not_found_user_detail",
                                new Long[]{id})));
        if (currentAuditor.isPresent()) {
            Long currentUserId = currentAuditor.get();
            if (appUser.getId().equals(currentUserId)) {
                throw new ForbiddenActionException(utils.getMessage("error_deactivate_own_user"));
            }
            appUser.setActive(false);
            appUserRepository.save(appUser);
        } else {
            throw new AccessDeniedException(utils.getMessage("error_auditor_empty"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void deactivateUser(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        var appUser = appUserRepository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException(
                        utils.getMessage(
                                "exception_id_not_found_user_detail",
                                new Long[]{id})));
        if (appUser.getRole() == AppUserRole.USER && currentAuditor.isPresent()) {
            appUser.setActive(false);
            appUserRepository.save(appUser);
        } else {
            throw new AccessDeniedException(utils.getMessage("error_deactivate_user"));
        }
    }

}
