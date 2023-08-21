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

package com.duberlyguarnizo.dummyjson.config;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableJpaAuditing
@EnableMethodSecurity
@EnableScheduling
@Slf4j
public class Beans {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AppUserRepository repository;

    @Value("${firstAdmin.username}")
    String firstAdminUsername;
    @Value("${firstAdmin.password}")
    String firstAdminPassword;


    public Beans(AuthenticationConfiguration authenticationConfiguration, AppUserRepository repository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.repository = repository;

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CommandLineRunner initUser() {

        return args -> {
            var matchingFirstUsers = repository.countAppUserByRole(AppUserRole.ADMIN);
            if (matchingFirstUsers == 0) {
                var firstUser = AppUser.builder()
                        .username(firstAdminUsername)
                        .password(passwordEncoder().encode(firstAdminPassword))
                        .email("duberlygfr@gmail.com")
                        .names("Delete This Admin After Creation")
                        .idCard("no-id-card")
                        .role(AppUserRole.ADMIN)
                        .active(true)
                        .locked(false)
                        .build();
                try {
                    repository.save(firstUser);
                } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
                    log.warn("Error saving first user: {}", e.getMessage());
                }
            } else {
                log.info("At least one AppUser with role ADMIN exists. No need to create a first admin.");
            }
        };
    }

}
