package com.duberlyguarnizo.dummyjson.jwt_token;

import com.duberlyguarnizo.dummyjson.exceptions.ForbiddenActionException;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final ControllerUtils utils;

    public List<JwtToken> getTokenByUserId(Long userId) {
        var tokenList = tokenRepository.findByUserId(userId);
        if (tokenList.isEmpty()) {
            throw new IdNotFoundException("There are no tokens for user with id " + userId);
        } else {
            return tokenList;
        }
    }

    public JwtToken getTokenByJwtString(String jwt) {
        var token = tokenRepository.findByToken(jwt);
        if (token.isEmpty()) {
            throw new IdNotFoundException("There is no tokens registered with this JWT");
        } else {
            return token.get();
        }
    }

    public void saveToken(String jwt, Long userId) {
        JwtToken jwtToken = JwtToken.builder()
                .token(jwt)
                .userId(userId)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(jwtToken);
    }

    public void revokeToken(String headerToken) {
        String username = null;
        String jwt = null;

        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            jwt = headerToken.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // username and authentication exists, so logout:
            var token = getTokenByJwtString(jwt);
            token.setRevoked(true);
        } else {
            throw new ForbiddenActionException(utils.getMessage("error_auditor_empty"));
        }
    }
}
