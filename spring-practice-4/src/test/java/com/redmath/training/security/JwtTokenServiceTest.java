package com.redmath.training.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

  @Mock
  private JwtEncoder jwtEncoder;

  @Mock
  private JwtDecoder jwtDecoder;

  private JwtTokenService jwtTokenService;

  @BeforeEach
  void setUp() {
    jwtTokenService = new JwtTokenService(jwtEncoder, jwtDecoder);
  }

  @Test
  void generateToken_shouldIncludeSubjectAndScopes() {
    Authentication auth = new UsernamePasswordAuthenticationToken(
        "testuser", null,
        List.of(new SimpleGrantedAuthority("reporter"), new SimpleGrantedAuthority("editor")));

    Instant now = Instant.now();
    Jwt jwt = Jwt.withTokenValue("token-value")
        .header("alg", "PS256")
        .subject("testuser")
        .claim("scope", "reporter editor")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(900))
        .build();

    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    String token = jwtTokenService.generateToken(auth);

    assertThat(token).isEqualTo("token-value");
    verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
  }

  @Test
  void generateRefreshToken_shouldHaveRefreshScope() {
    Authentication auth = new UsernamePasswordAuthenticationToken(
        "testuser", null, List.of());

    Instant now = Instant.now();
    Jwt jwt = Jwt.withTokenValue("refresh-token")
        .header("alg", "PS256")
        .subject("testuser")
        .claim("scope", "REFRESH_TOKEN")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(604800))
        .build();

    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    String token = jwtTokenService.generateRefreshToken(auth);

    assertThat(token).isEqualTo("refresh-token");
  }

  @Test
  void decodeToken_shouldReturnJwt() {
    Jwt jwt = Jwt.withTokenValue("decoded-token")
        .header("alg", "PS256")
        .subject("testuser")
        .build();

    when(jwtDecoder.decode("decoded-token")).thenReturn(jwt);

    Jwt result = jwtTokenService.decodeToken("decoded-token");

    assertThat(result).isEqualTo(jwt);
    verify(jwtDecoder).decode("decoded-token");
  }

  @Test
  void generateToken_shouldThrowWhenEncoderFails() {
    Authentication auth = new UsernamePasswordAuthenticationToken(
        "testuser", null, List.of());

    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenThrow(
        new RuntimeException("Encoding failed"));

    assertThatThrownBy(() -> jwtTokenService.generateToken(auth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Encoding failed");
  }
}
