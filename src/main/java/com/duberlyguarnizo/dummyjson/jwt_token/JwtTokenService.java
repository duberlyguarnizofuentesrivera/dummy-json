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

package com.duberlyguarnizo.dummyjson.jwt_token;

import com.duberlyguarnizo.dummyjson.exceptions.ForbiddenActionException;
import com.duberlyguarnizo.dummyjson.exceptions.IdNotFoundException;
import com.duberlyguarnizo.dummyjson.exceptions.RepositoryException;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import com.duberlyguarnizo.dummyjson.util.ControllerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public void revokeAllCurrentUserTokens(String headerToken) {
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void revokeAllUserTokensByUserId(Long userId) {
        var tokenList = tokenRepository.findByUserId(userId);
        if (tokenList.isEmpty()) {
            throw new IdNotFoundException(utils.getMessage("exception_id_not_found_token_user", new Long[]{userId}));
        } else {
            try {
                tokenList.forEach(t -> tokenRepository.deleteById(t.getId()));
            } catch (IllegalArgumentException e) {
                throw new RepositoryException(utils.getMessage("exception_repository_save_error_token_revoke"));
            }
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
