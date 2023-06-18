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
            return possibleAppUser.get();
        }
    }
}
