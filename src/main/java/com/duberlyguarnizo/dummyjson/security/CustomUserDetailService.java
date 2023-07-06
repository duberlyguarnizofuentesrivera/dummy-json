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

package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final ControllerUtils utils;

    public CustomUserDetailService(AppUserRepository appUserRepository, ControllerUtils utils) {
        this.appUserRepository = appUserRepository;
        this.utils = utils;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var possibleAppUser = appUserRepository.findByUsernameIgnoreCase(username); //name must be unique!
        if (possibleAppUser.isEmpty()) {
            throw new UsernameNotFoundException(utils.getMessage("exception_username_not_found", new String[]{username}));
        } else {
            return possibleAppUser.get(0);
        }
    }
}
