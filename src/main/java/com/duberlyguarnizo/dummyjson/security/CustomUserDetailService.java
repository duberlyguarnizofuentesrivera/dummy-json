package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public CustomUserDetailService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var possibleEmployee = appUserRepository.findByUsernameIgnoreCase(username);
        if (possibleEmployee.isEmpty()) {
            throw new UsernameNotFoundException("The user with the name " + username + " could not be found!");
        } else {
            return possibleEmployee.get();
        }
    }
}
