/*
 * dummy-json
 * Copyright (c) 2023 Duberly Guarnizo Fuentes Rivera <duberlygfr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duberlyguarnizo.dummyjson.appuser;

import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserBasicDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserDetailDto;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserMapper;
import com.duberlyguarnizo.dummyjson.appuser.dto.AppUserRegistrationDto;
import com.duberlyguarnizo.dummyjson.auditing.CustomAuditorAware;
import com.duberlyguarnizo.dummyjson.exceptions.ForbiddenActionException;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.InvalidFieldValueException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenService;
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
    private final JwtTokenService jwtService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public AppUserDetailDto getManagerById(Long id) {
        return mapper.toDetailDto(
                findAppUserById(id, true));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<AppUserBasicDto> getAllManagers(Pageable page) {
        return appUserRepository.findAll(page).map(mapper::toBasicDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can create users
    public Long createManager(@Valid AppUserRegistrationDto registrationDto) throws RepositoryException {
        registrationDto.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        //Verify the role. A manager cannot have role USER
        if (registrationDto.getRole() != AppUserRole.ADMIN && registrationDto.getRole() != AppUserRole.SUPERVISOR) {
            throw new InvalidFieldValueException("The managers can only have ADMIN or SUPERVISOR role");//TODO: translate this
        }
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
    public void partialUpdateManager(AppUserRegistrationDto registrationDto) {
        if (registrationDto.getRole() != AppUserRole.ADMIN && registrationDto.getRole() != AppUserRole.SUPERVISOR) {
            throw new InvalidFieldValueException("The managers can only have ADMIN or SUPERVISOR role");//TODO: translate this
        }
        var manager = findAppUserById(registrationDto.getId(), true);
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
            var appUser = findAppUserById(id, true);
            Long currentUserId = currentAuditor.get();
            if (appUser.getId().equals(currentUserId)) {
                throw new ForbiddenActionException(utils.getMessage("error_delete_own_user"));
            }
            appUserRepository.deleteById(id);
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void deleteUser(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        if (currentAuditor.isEmpty()) {
            throw new AccessDeniedException(utils.getMessage("error_auditor_empty"));
        } else {
            var appUser = findAppUserById(id, false);
            Long currentUserId = currentAuditor.get();
            if (appUser.getId().equals(currentUserId)) {
                throw new ForbiddenActionException(utils.getMessage("error_delete_own_user"));
            }
            if (appUser.getRole() == AppUserRole.ADMIN || appUser.getRole() == AppUserRole.SUPERVISOR) {
                throw new ForbiddenActionException(utils.getMessage("error_delete_user"));
            }
            appUserRepository.deleteById(id);
        }
    }

    /**
     * Deactivates a manager user by id. Only an ADMIN manager can deactivate other managers.
     *
     * @param id The id of the manager user to be deactivated.
     * @throws IdNotFoundException      If no user is found with the provided id.
     * @throws ForbiddenActionException If the current user tries to deactivate their own account or a non
     *                                  manager user's account.
     * @throws AccessDeniedException    If auditor is not present in the current context.
     */
    @PreAuthorize("hasAuthority('ADMIN')") //only admin user can deactivate other admins
    public void deactivateManager(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        var appUser = findAppUserById(id, true);
        if (currentAuditor.isPresent()) {
            Long currentUserId = currentAuditor.get();
            if (appUser.getId().equals(currentUserId)) {
                throw new ForbiddenActionException(utils.getMessage("error_deactivate_own_user"));
            }
            if (appUser.getRole() != AppUserRole.ADMIN && appUser.getRole() != AppUserRole.SUPERVISOR) {
                //We use both options in case a fork adds new roles besides USER
                throw new ForbiddenActionException(utils.getMessage("error_deactivate_manager"));
            }
            appUser.setActive(false);
            appUserRepository.save(appUser);
            // revoke JWT for the deactivated user
            jwtService.revokeAllUserTokensByUserId(appUser.getId());

        } else {
            throw new AccessDeniedException(utils.getMessage("error_auditor_empty"));
        }
    }

    /**
     * Deactivates a user by id. Only an ADMIN or SUPERVISOR can deactivate non-manager users.
     *
     * @param id The id of the user to be deactivated.
     * @throws IdNotFoundException   If no user is found with the provided id.
     * @throws AccessDeniedException If the current user is not authorized to deactivate the user or if auditor is not present in the current context.
     */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void deactivateUser(Long id) {
        var currentAuditor = auditorAware.getCurrentAuditor();
        var appUser = findAppUserById(id, false);
        if (appUser.getRole() != AppUserRole.ADMIN
                && appUser.getRole() != AppUserRole.SUPERVISOR
                && currentAuditor.isPresent()) {
            appUser.setActive(false);
            appUserRepository.save(appUser);
            jwtService.revokeAllUserTokensByUserId(appUser.getId());
        } else {
            throw new AccessDeniedException(utils.getMessage("error_deactivate_user"));
        }
    }

    /**
     * Finds and returns an AppUser by its id. If not found, the exception is localized according to <i>isManager</i> flag.
     *
     * @param id        The id of the AppUser to be found.
     * @param isManager A boolean value to indicate whether the AppUser to be found is a manager or not.
     * @return An instance of AppUser with the provided id.
     * @throws IdNotFoundException If no AppUser is found with the provided id.
     */
    private AppUser findAppUserById(Long id, boolean isManager) {
        String i18n = "exception_id_not_found_user_detail";
        if (isManager) {
            i18n = "exception_id_not_found_manager_detail";
        }
        final String i18nString = i18n;
        return appUserRepository
                .findById(id)
                .orElseThrow(() -> new IdNotFoundException(
                        utils.getMessage(
                                i18nString,
                                new Long[]{id})));
    }

}
