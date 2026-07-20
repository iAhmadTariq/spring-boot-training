package com.redmath.training.security.auth;

import com.redmath.training.security.JwtTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public AuthService(JwtTokenService jwtTokenService, UserDetailsService userDetailsService){
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be empty");
        }

        try {
            Jwt jwt = jwtTokenService.decodeToken(refreshToken);
            String username = jwt.getSubject();
            String scope = jwt.getClaimAsString("scope");

            if (!"REFRESH_TOKEN".equals(scope)) {
                throw new JwtException("Invalid token purpose");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return jwtTokenService.generateToken(authentication);

        } catch (JwtException e) {
            throw new JwtException("Invalid or expired refresh token", e);
        }
    }
}
