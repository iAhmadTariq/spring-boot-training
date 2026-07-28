package com.redmath.training.security;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
  }

  public String generateToken(Authentication auth) {
    String scope = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));

    return buildToken(auth.getName(), scope, 15, ChronoUnit.MINUTES);
  }

  public String generateRefreshToken(Authentication auth) {
    return buildToken(auth.getName(), "REFRESH_TOKEN", 7, ChronoUnit.DAYS);
  }

  private String buildToken(String subject, String scope, long amount, ChronoUnit unit) {
    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(amount, unit))
        .subject(subject)
        .claim("scope", scope)
        .build();
    JwsHeader header = JwsHeader.with(SignatureAlgorithm.PS256).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public Jwt decodeToken(String token) {
    return jwtDecoder.decode(token);
  }
}
