package com.redmath.training.security;

import com.redmath.training.user.service.ApiUserService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class ApiSecurityService {

  private static final Logger log = LoggerFactory.getLogger(ApiSecurityService.class);
  private final ApiUserService apiUserService;
  private final JwtTokenService jwtTokenService;
  private final ObjectMapper objectMapper;

  public ApiSecurityService(ApiUserService apiUserService, JwtTokenService jwtTokenService,
      ObjectMapper objectMapper) {
    this.apiUserService = apiUserService;
    this.jwtTokenService = jwtTokenService;
    this.objectMapper = objectMapper;
  }

  public void onAuthenticationSuccessForm(JwtTokenService jwtTokenService,
      HttpServletResponse response, Authentication auth)
      throws IOException {
    String accessToken = jwtTokenService.generateToken(auth);
    String refreshToken = jwtTokenService.generateRefreshToken(auth);

    writeJsonResponse(response, accessToken, refreshToken);
  }

  public void onAuthenticationSuccessOauth(JwtTokenService jwtTokenService,
      HttpServletResponse response, Authentication auth)
      throws IOException {
    if (auth instanceof OAuth2AuthenticationToken oauthToken) {
      String email = getEmail(auth, oauthToken);
      apiUserService.provisionOauth2User(email);
      String accessToken = jwtTokenService.generateToken(auth);
      String refreshToken = jwtTokenService.generateRefreshToken(auth);

      writeJsonResponse(response, accessToken, refreshToken);
    }
  }

  private void writeJsonResponse(HttpServletResponse response, String accessToken,
      String refreshToken)
      throws IOException {
    Map<String, String> tokenResponse = Map.of(
        "access_token", accessToken,
        "refresh_token", refreshToken
    );

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), tokenResponse);
  }

  private static String getEmail(Authentication authentication,
      OAuth2AuthenticationToken oauthToken) {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    if (oauth2User == null) {
      throw new IllegalArgumentException("OAuth2 user principal is null");
    }
    String provider = oauthToken.getAuthorizedClientRegistrationId();
    String email;
    if ("github".equals(provider)) {
      email = oauth2User.getAttribute("login");
    } else {
      email = oauth2User.getAttribute("email");
    }
    return email;
  }

  public DefaultOAuth2AuthenticatedPrincipal authenticateRequest(String token) {
    Jwt jwt = jwtTokenService.decodeToken(token);

    String username = jwt.getSubject();
    String roles = jwt.getClaimAsString("scope");

    return new DefaultOAuth2AuthenticatedPrincipal(
        username,
        Map.of("sub", username),
        AuthorityUtils.createAuthorityList(roles.split(" "))
    );
  }

}