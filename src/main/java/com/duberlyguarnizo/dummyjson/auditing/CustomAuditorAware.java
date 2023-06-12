package com.duberlyguarnizo.dummyjson.auditing;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuditorAware implements AuditorAware<Long> {
    private final AppUserRepository appUserRepository;

    public CustomAuditorAware(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public @NotNull Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        } else {
            AppUser principal =
                    (AppUser) authentication
                            .getPrincipal();
            String username = principal.getUsername();
            Optional<AppUser> currentUser = appUserRepository.findByUsernameIgnoreCase(username);
            return currentUser.map(AppUser::getId);
        }
    }
}