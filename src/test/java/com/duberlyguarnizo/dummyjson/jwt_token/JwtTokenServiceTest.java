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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {
    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtTokenRepository tokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ControllerUtils utils;

    @AfterAll
    public static void destroy() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testUser", "passwd"));
    }

    @Test
    void getTokenByUserIdReturnsCorrectToken() {
        Long userId = 1L;

        JwtToken token1 = new JwtToken();
        // Set any required properties on the JwtToken
        JwtToken token2 = new JwtToken();
        // Set any required properties on the JwtToken

        List<JwtToken> tokenList = new ArrayList<>();
        tokenList.add(token1);
        tokenList.add(token2);

        when(tokenRepository.findByUserId(userId)).thenReturn(tokenList);

        List<JwtToken> result = jwtTokenService.getTokensByUserId(userId);

        Assertions.assertNotNull(result);
        assertEquals(tokenList.size(), result.size());
    }

    @Test
    void TestNameForEmptyTokenListScenario() {
        Long userId = 1L;

        List<JwtToken> tokenList = new ArrayList<>();

        when(tokenRepository.findByUserId(userId)).thenReturn(tokenList);

        Throwable exception = assertThrows(IdNotFoundException.class, () -> {
            jwtTokenService.getTokensByUserId(userId);
        });
    }

    @Test
    void testGetTokenByJwtStringSuccessfulScenario() {
        String jwt = "jwt123";

        JwtToken token = new JwtToken();
        // Set any required properties on the JwtToken

        Optional<JwtToken> optionalToken = Optional.of(token);

        when(tokenRepository.findByToken(jwt)).thenReturn(optionalToken);

        JwtToken result = jwtTokenService.getTokenByJwtString(jwt);

        Assertions.assertNotNull(result);
        // Add more assertions to validate result
    }

    @Test
    void testGetTokenByJwtStringTokenNotFoundScenario() {
        String jwt = "invalidJwt";

        Optional<JwtToken> optionalToken = Optional.empty();

        when(tokenRepository.findByToken(jwt)).thenReturn(optionalToken);

        Throwable exception = assertThrows(IdNotFoundException.class, () -> {
            jwtTokenService.getTokenByJwtString(jwt);
        });
    }

    @Test
    void testSaveToken() {
        String jwt = "jwt123";
        Long userId = 1L;

        JwtToken jwtToken = JwtToken.builder()
                .token(jwt)
                .userId(userId)
                .expired(false)
                .revoked(false)
                .createdDate(LocalDateTime.now())
                .build();

        jwtTokenService.saveToken(jwt, userId);

        Mockito.verify(tokenRepository, Mockito.times(1)).save(any());
    }

    @Test
    void revokeTokenSuccessTest() {
        // Setup
        String testToken = "Bearer testToken";
        when(jwtUtil.extractUsername(anyString())).thenReturn("testUser");
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(new JwtToken()));

        // Execute
        jwtTokenService.revokeToken(testToken);

        // Verify
        verify(tokenRepository, times(1)).save(any(JwtToken.class));
    }

    @Test
    void revokeTokenExceptionTest() {
        // Setup
        String testToken = "Bearer testToken";
        when(jwtUtil.extractUsername(anyString())).thenReturn(null);

        // Execute
        assertThrows(ForbiddenActionException.class, () -> jwtTokenService.revokeToken(testToken));

    }

    @Test
    void revokeAllCurrentUserTokensSuccessTest() {
        // Setup
        String testToken = "Bearer testToken";
        when(jwtUtil.extractUsername(anyString())).thenReturn("testUser");
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(new JwtToken()));
        when(tokenRepository.findByUserId(any())).thenReturn(Arrays.asList(new JwtToken(), new JwtToken()));

        // Execute
        jwtTokenService.revokeAllCurrentUserTokens(testToken);

        // Verify
        verify(tokenRepository, times(2)).save(any(JwtToken.class));
    }

    @Test
    void revokeAllCurrentUserTokensExceptionTest() {
        // Setup
        String testToken = "Bearer testToken";
        when(jwtUtil.extractUsername(anyString())).thenReturn(null);

        // Execute
        assertThrows(ForbiddenActionException.class, () -> jwtTokenService.revokeAllCurrentUserTokens(testToken));
    }

    @Test
    void revokeAllUserTokensByUserIdSuccessTest() {
        // Setup
        Long userId = 1L;
        List<JwtToken> tokens = Arrays.asList(new JwtToken(), new JwtToken());
        when(tokenRepository.findByUserId(userId)).thenReturn(tokens);

        // Execute
        jwtTokenService.revokeAllUserTokensByUserId(userId);

        // Verify
        verify(tokenRepository, times(tokens.size())).deleteById(any());
    }

    @Test
    void revokeAllUserTokensByUserIdIdNotFoundExceptionTest() {
        // Setup
        Long userId = 1L;
        when(tokenRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(utils.getMessage(anyString(), any())).thenReturn("exception_id_not_found_token_user");

        // Execute
        Exception exception = assertThrows(IdNotFoundException.class, () -> jwtTokenService.revokeAllUserTokensByUserId(userId));

        // Verify
        assertEquals("exception_id_not_found_token_user", exception.getMessage());
    }

    @Test
    void revokeAllUserTokensByUserIdRepositoryExceptionTest() {
        // Setup
        Long userId = 1L;
        List<JwtToken> tokens = Arrays.asList(new JwtToken(), new JwtToken());
        when(tokenRepository.findByUserId(userId)).thenReturn(tokens);
        doThrow(IllegalArgumentException.class).when(tokenRepository).deleteById(any());
        // Execute
        assertThrows(RepositoryException.class, () -> jwtTokenService.revokeAllUserTokensByUserId(userId));

    }

}