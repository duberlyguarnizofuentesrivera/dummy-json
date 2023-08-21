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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
        // To avoid stack overflow exception with AuditorAware
// See https://stackoverflow.com/questions/42315960/stackoverflowexception-in-spring-data-jpa-app-with-spring-security-auditoraware
// to review a better way, but harder, to do this... (Duberly Guarnizo, 2023).
    List<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByUsernameContainsIgnoreCase(String username);

    Integer countAppUserByRole(AppUserRole role);

    Page<AppUser> findByRoleIn(List<AppUserRole> roles, Pageable page);

    Page<AppUser> findByRole(AppUserRole role, Pageable page);
}
