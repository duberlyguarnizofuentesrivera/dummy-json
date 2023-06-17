package com.duberlyguarnizo.dummyjson.appuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
        // To avoid stack overflow exception with AuditorAware
// See https://stackoverflow.com/questions/42315960/stackoverflowexception-in-spring-data-jpa-app-with-spring-security-auditoraware
// to review a better way, but harder, to do this... (Duberly Guarnizo, 2023).
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByUsernameContainsIgnoreCase(String username);
}
