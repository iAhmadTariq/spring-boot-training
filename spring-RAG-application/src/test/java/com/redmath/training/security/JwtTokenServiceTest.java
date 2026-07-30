package com.redmath.training.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

  @Mock
  private JwtEncoder jwtEncoder;

  @Mock
  private JwtDecoder jwtDecoder;

  @Test
  void generatesAccessTokenWithAuthoritiesScope() {
    JwtTokenService service = new JwtTokenService(jwtEncoder, jwtDecoder);
    Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
        "alice",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
    );

    when(jwtEncoder.encode(any())).thenAnswer(invocation ->
        Jwt.withTokenValue("access-token")
            .header("alg", "PS256")
            .subject("alice")
            .claim("scope", "ROLE_USER ROLE_ADMIN")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build());

    String token = service.generateToken(auth);

    assertThat(token).isEqualTo("access-token");

    ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(
        JwtEncoderParameters.class);
    org.mockito.Mockito.verify(jwtEncoder).encode(captor.capture());
    JwtClaimsSet claims = captor.getValue().getClaims();
    assertThat(claims.getSubject()).isEqualTo("alice");
    assertThat(claims.getClaims()).containsEntry("iss", "self");
    assertThat(claims.getClaimAsString("scope")).isEqualTo("ROLE_USER ROLE_ADMIN");
    assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
  }

  @Test
  void generatesRefreshTokenWithRefreshScope() {
    JwtTokenService service = new JwtTokenService(jwtEncoder, jwtDecoder);
    Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
        "alice",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    when(jwtEncoder.encode(any())).thenReturn(
        Jwt.withTokenValue("refresh-token")
            .header("alg", "PS256")
            .subject("alice")
            .claim("scope", "REFRESH_TOKEN")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build());

    assertThat(service.generateRefreshToken(auth)).isEqualTo("refresh-token");
  }

  @Test
  void delegatesDecodingToJwtDecoder() {
    JwtTokenService service = new JwtTokenService(jwtEncoder, jwtDecoder);
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("alice")
        .claim("scope", "ROLE_USER")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
    when(jwtDecoder.decode("token")).thenReturn(jwt);

    assertThat(service.decodeToken("token")).isSameAs(jwt);
  }
}
