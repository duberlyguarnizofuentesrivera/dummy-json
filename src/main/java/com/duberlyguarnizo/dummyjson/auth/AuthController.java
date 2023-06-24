package com.duberlyguarnizo.dummyjson.auth;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.exceptions.JwtValidationException;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenService;
import com.duberlyguarnizo.dummyjson.security.CustomUserDetailService;
import com.duberlyguarnizo.dummyjson.security.JwtUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoint for login")
@Log
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService userDetailService;
    private final JwtTokenService tokenService; //Using repository directly, to avoid use of intermediate layer for speed
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, CustomUserDetailService userDetailService, JwtTokenService tokenService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> doAuthenticate(@RequestBody AuthRequest request) throws AuthenticationException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        final AppUser appUser = (AppUser) userDetailService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(appUser);
        AuthResponse response = new AuthResponse(jwt);

        tokenService.saveToken(jwt, appUser.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> doLogOut(@RequestHeader("authorization") String headerToken) {
        tokenService.revokeToken(headerToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Revokes all authorization tokens for the user, effectively logging out sessions on all devices.
     *
     * @param headerToken The JWT included in the request, as an authorization header
     * @return HTTP 202 NO CONTENT if successful, or ProblemDetail if an exception is thrown.
     */
    @GetMapping("/logout-all")
    public ResponseEntity<Void> doLogOutAllSessions(@RequestHeader("authorization") String headerToken) {
        tokenService.revokeAllUserTokens(headerToken);
        return ResponseEntity.noContent().build();
    }

    @Hidden //no need to show this as is a utility endpoint, not designed to be used by API clients
    @GetMapping("/invalid-jwt")
    public ResponseEntity<Void> returnInvalidJwtProblemDetail() {
        throw new JwtValidationException();
    }

}
