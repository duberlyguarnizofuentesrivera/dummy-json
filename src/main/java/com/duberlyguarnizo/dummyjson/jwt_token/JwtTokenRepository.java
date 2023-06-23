package com.duberlyguarnizo.dummyjson.jwt_token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    List<JwtToken> findByUserId(Long id);

    Optional<JwtToken> findByToken(String token);

}
