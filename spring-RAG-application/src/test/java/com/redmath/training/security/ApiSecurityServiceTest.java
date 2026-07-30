package com.redmath.training.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.redmath.training.user.ApiUserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ApiSecurityServiceTest {

  @Mock
  private ApiUserService apiUserService;

  @Mock
  private JwtTokenService jwtTokenService;

  @Test
  void writesTokenResponseAfterSuccessfulAuthentication() throws Exception {
    ApiSecurityService service = new ApiSecurityService(apiUserService, jwtTokenService,
        new ObjectMapper());
    Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
        "alice",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtTokenService.generateToken(any())).thenReturn("access-token");
    when(jwtTokenService.generateRefreshToken(any())).thenReturn("refresh-token");

    service.onAuthenticationSuccessForm(jwtTokenService, response, auth);

    assertThat(response.getContentType()).startsWith("application/json");
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(response.getContentAsString()).contains("\"access_token\":\"access-token\"");
    assertThat(response.getContentAsString()).contains("\"refresh_token\":\"refresh-token\"");
  }

  @Test
  void authenticatesRequestFromJwtClaims() {
    ApiSecurityService service = new ApiSecurityService(apiUserService, jwtTokenService,
        new ObjectMapper());
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("alice")
        .claim("scope", "ROLE_USER ROLE_ADMIN")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
    when(jwtTokenService.decodeToken("token")).thenReturn(jwt);

    var principal = service.authenticateRequest("token");

    assertThat(principal.getName()).isEqualTo("alice");
    assertThat(principal.getAttributes()).containsEntry("sub", "alice");
    assertThat(principal.getAuthorities())
        .extracting(authority -> authority.getAuthority())
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }
}
