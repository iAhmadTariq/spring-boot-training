package com.redmath.training.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.training.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private JwtTokenService jwtTokenService;

  @Mock
  private UserDetailsService userDetailsService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(jwtTokenService, userDetailsService);
  }

  @Test
  void refreshAccessToken_shouldReturnNewToken_whenRefreshTokenIsValid() {
    UserDetails userDetails = User.withUsername("testuser")
        .password("{noop}password")
        .authorities("reporter", "editor")
        .build();

    Jwt jwt = Jwt.withTokenValue("refresh-token")
        .header("alg", "PS256")
        .subject("testuser")
        .claim("scope", "REFRESH_TOKEN")
        .build();

    when(jwtTokenService.decodeToken("valid-refresh-token")).thenReturn(jwt);
    when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
    when(jwtTokenService.generateToken(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn("new-access-token");

    String newToken = authService.refreshAccessToken("valid-refresh-token");

    assertThat(newToken).isEqualTo("new-access-token");
    verify(jwtTokenService).generateToken(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void refreshAccessToken_shouldThrow_whenRefreshTokenIsNull() {
    assertThatThrownBy(() -> authService.refreshAccessToken(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Refresh token cannot be empty");
  }

  @Test
  void refreshAccessToken_shouldThrow_whenRefreshTokenIsBlank() {
    assertThatThrownBy(() -> authService.refreshAccessToken("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Refresh token cannot be empty");
  }

  @Test
  void refreshAccessToken_shouldThrow_whenTokenHasInvalidScope() {
    Jwt jwt = Jwt.withTokenValue("access-token")
        .header("alg", "PS256")
        .subject("testuser")
        .claim("scope", "reporter")  // missing "refresh" scope
        .build();

    when(jwtTokenService.decodeToken("access-token")).thenReturn(jwt);

    assertThatThrownBy(() -> authService.refreshAccessToken("access-token"))
        .isInstanceOf(JwtException.class)
        .hasMessage("Invalid or expired refresh token");  // <-- changed here
  }

  @Test
  void refreshAccessToken_shouldThrow_whenTokenDecodingFails() {
    when(jwtTokenService.decodeToken("invalid-token"))
        .thenThrow(new JwtException("Invalid token"));

    assertThatThrownBy(() -> authService.refreshAccessToken("invalid-token"))
        .isInstanceOf(JwtException.class)
        .hasMessage("Invalid or expired refresh token");
  }

  @Test
  void refreshAccessToken_shouldThrow_whenUserNotFound() {
    Jwt jwt = Jwt.withTokenValue("refresh-token")
        .header("alg", "PS256")
        .subject("nonexistent")
        .claim("scope", "REFRESH_TOKEN")
        .build();

    when(jwtTokenService.decodeToken("refresh-token")).thenReturn(jwt);
    when(userDetailsService.loadUserByUsername("nonexistent"))
        .thenThrow(new UsernameNotFoundException("User not found"));

    assertThatThrownBy(() -> authService.refreshAccessToken("refresh-token"))
        .isInstanceOf(UsernameNotFoundException.class)  // <-- changed
        .hasMessage("User not found");
  }
}
