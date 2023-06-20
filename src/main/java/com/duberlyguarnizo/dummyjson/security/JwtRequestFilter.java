package com.duberlyguarnizo.dummyjson.security;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.exceptions.JwtValidationException;
import com.duberlyguarnizo.dummyjson.jwt_token.JwtTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final CustomUserDetailService userDetailService;
    private final JwtUtil jwtUtil;
    private final JwtTokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            AppUser appUser = (AppUser) userDetailService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, appUser)) {
                var token = tokenRepository.findByToken(jwt).orElse(null); //jwt has been validated already

                if (token != null) {
                    if (!token.isExpired() && !token.isRevoked()) {
                        UsernamePasswordAuthenticationToken upAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        appUser,
                                        null,
                                        appUser.getAuthorities()
                                );
                        //TODO: validate token is not expired or revoked
                        upAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(upAuthenticationToken);
                    } else {
                        throw new JwtValidationException();
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
