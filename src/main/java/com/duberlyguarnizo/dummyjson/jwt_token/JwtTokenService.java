package com.duberlyguarnizo.dummyjson.jwt_token;

import com.duberlyguarnizo.dummyjson.exceptions.ForbiddenActionException;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final ControllerUtils utils;


    private static final int EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    public List<JwtToken> getTokensByUserId(Long userId) {
        var tokenList = tokenRepository.findByUserId(userId);
        if (tokenList.isEmpty()) {
            throw new IdNotFoundException(utils.getMessage("exception_id_not_found_token_user", new Long[]{userId}));
        } else {
            return tokenList;
        }
    }

    public JwtToken getTokenByJwtString(String jwt) {
        var token = tokenRepository.findByToken(jwt);
        if (token.isEmpty()) {
            throw new IdNotFoundException(utils.getMessage("exception_id_not_found_token_jwt"));
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
                .createdDate(LocalDateTime.now())
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
        if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            // username and authentication exists, so logout:
            var token = getTokenByJwtString(jwt);
            token.setRevoked(true);
            tokenRepository.save(token);
        } else {
            throw new ForbiddenActionException(utils.getMessage("error_auditor_empty"));
        }
    }

    public void revokeAllUserTokens(String headerToken) {
        String username = null;
        String jwt = null;

        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            jwt = headerToken.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            // username and authentication exists, so logout:
            var token = getTokenByJwtString(jwt);
            var allTokens = tokenRepository.findByUserId(token.getUserId());
            for (JwtToken tk : allTokens) {
                tk.setRevoked(true);
                tokenRepository.save(tk);
            }

        } else {
            throw new ForbiddenActionException(utils.getMessage("error_auditor_empty"));
        }
    }

    // Expire tokens every 5 hours so no token exists with more than 10 hours
    @Scheduled(fixedDelay = EXPIRATION_TIME / 2)
    public void scheduledTokenExpirationTask() {
        var tenHoursAgo = LocalDateTime.now().minusHours(10);
        var expiredTokens = tokenRepository.findByCreatedDateBefore(tenHoursAgo);
        for (JwtToken token : expiredTokens) {
            token.setExpired(true);
            tokenRepository.save(token);
        }
    }

    // Delete tokens from database after 2 days
    @Scheduled(fixedDelay = EXPIRATION_TIME)
    public void scheduleTokenDeleteTask() {
        var twoDaysAgo = LocalDateTime.now().minusHours(48);
        var expiredTokens = tokenRepository.findByCreatedDateBefore(twoDaysAgo);
        for (JwtToken token : expiredTokens) {
            if (token.isExpired()) {
                tokenRepository.deleteById(token.getId());
            }
        }
    }

}
