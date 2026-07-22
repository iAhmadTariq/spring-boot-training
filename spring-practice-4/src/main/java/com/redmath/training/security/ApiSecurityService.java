package com.redmath.training.security;

import com.redmath.training.user.service.ApiUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ApiSecurityService {

  private final ApiUserService apiUserService;
  private final JwtTokenService jwtTokenService;

  public ApiSecurityService(ApiUserService apiUserService, JwtTokenService jwtTokenService) {
    this.apiUserService = apiUserService;
    this.jwtTokenService = jwtTokenService;
  }

  public void onAuthenticationSuccessForm(JwtTokenService jwtTokenService,
      HttpServletRequest request, HttpServletResponse response, Authentication auth)
      throws IOException {
    String accessToken = jwtTokenService.generateToken(auth);
    String refreshToken = jwtTokenService.generateRefreshToken(auth);

    response.setContentType("application/json");
    response.getWriter().write(
        String.format("{\"access_token\":\"%s\", {\"refresh_token\":\"%s\"}", accessToken,
            refreshToken));
  }

  public void onAuthenticationSuccessOauth(JwtTokenService jwtTokenService,
      HttpServletRequest request, HttpServletResponse response, Authentication auth)
      throws IOException {
    if (auth instanceof OAuth2AuthenticationToken oauthToken) {
      String email = getEmail(auth, oauthToken);
      apiUserService.provisionOauth2User(email);
      String accessToken = jwtTokenService.generateToken(auth);
      String refreshToken = jwtTokenService.generateRefreshToken(auth);

      response.setContentType("application/json");
      response.getWriter().write(
          String.format("{\"access_token\":\"%s\", {\"refresh_token\":\"%s\"}", accessToken,
              refreshToken));
    }
  }

  private static String getEmail(Authentication authentication,
      OAuth2AuthenticationToken oauthToken) {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    String provider = oauthToken.getAuthorizedClientRegistrationId();
    String email;
    assert oauth2User != null;
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
