package com.redmath.training.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.training.user.service.ApiUserService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ApiSecurityServiceTest {

  @Mock
  private ApiUserService apiUserService;

  @Mock
  private JwtTokenService jwtTokenService;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private HttpServletResponse response;

  private ApiSecurityService apiSecurityService;
  private Method getEmailMethod;

  @BeforeEach
  void setUp() throws Exception {
    apiSecurityService = new ApiSecurityService(apiUserService, jwtTokenService, objectMapper);
    getEmailMethod = ApiSecurityService.class.getDeclaredMethod("getEmail", Authentication.class,
        OAuth2AuthenticationToken.class);
    getEmailMethod.setAccessible(true);
  }


  @Test
  void onAuthenticationSuccessOauth_shouldDoNothing_whenAuthIsNotOAuth2() throws IOException {
    Authentication auth = new UsernamePasswordAuthenticationToken(
        "testuser", null, List.of());

    apiSecurityService.onAuthenticationSuccessOauth(jwtTokenService, response, auth);

    verify(apiUserService, never()).provisionOauth2User(any());
    verify(jwtTokenService, never()).generateToken(any());
  }

  @Test
  void authenticateRequest_shouldReturnPrincipalWithAuthorities() {
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "PS256")
        .subject("testuser")
        .claim("scope", "reporter editor")
        .build();

    when(jwtTokenService.decodeToken("token")).thenReturn(jwt);

    var principal = apiSecurityService.authenticateRequest("token");

    assertThat(principal.getName()).isEqualTo("testuser");
    assertThat(principal.getAuthorities()).hasSize(2);
  }

  @Test
  void getEmail_shouldReturnEmailForNonGithubProvider() throws Exception {
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(),
        Map.of("email", "user@example.com"),
        "email"
    );

    Authentication auth = new UsernamePasswordAuthenticationToken(
        oauth2User, null, List.of());
    OAuth2AuthenticationToken oauthToken = new OAuth2AuthenticationToken(
        oauth2User, List.of(), "google");

    String email = (String) getEmailMethod.invoke(apiSecurityService, auth, oauthToken);

    assertThat(email).isEqualTo("user@example.com");
  }

  @Test
  void getEmail_shouldReturnLoginForGithubProvider() throws Exception {
    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(),
        Map.of("login", "githubuser"),
        "login"
    );

    Authentication auth = new UsernamePasswordAuthenticationToken(
        oauth2User, null, List.of());
    OAuth2AuthenticationToken oauthToken = new OAuth2AuthenticationToken(
        oauth2User, List.of(), "github");

    String email = (String) getEmailMethod.invoke(apiSecurityService, auth, oauthToken);

    assertThat(email).isEqualTo("githubuser");
  }
}
