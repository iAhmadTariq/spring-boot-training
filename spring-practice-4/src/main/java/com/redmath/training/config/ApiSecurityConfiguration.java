package com.redmath.training.config;

import com.redmath.training.security.ApiSecurityService;
import com.redmath.training.security.JwtTokenService;
import com.redmath.training.user.service.ApiUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfiguration {

  private static final String API_V1_NEWS = "/api/v1/news";
  private static final String API_V1_NEWS_WILDCARD = "/api/v1/news/*";
  private static final String AUTHORITY_REPORTER = "reporter";
  private static final String AUTHORITY_EDITOR = "editor";

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, ApiUserService userService,
      JwtTokenService jwtTokenService,
      ApiUserService apiUserService,
      ApiSecurityService apiSecurityService) {
    http.authorizeHttpRequests(config -> config
            .requestMatchers("/api/v1/auth/*").permitAll()
            .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico")
            .permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, API_V1_NEWS, API_V1_NEWS_WILDCARD).permitAll()
            .requestMatchers(HttpMethod.POST, API_V1_NEWS)
            .hasAnyAuthority(AUTHORITY_REPORTER, AUTHORITY_EDITOR)
            .requestMatchers(HttpMethod.PUT, API_V1_NEWS_WILDCARD)
            .hasAnyAuthority(AUTHORITY_REPORTER, AUTHORITY_EDITOR)
            .requestMatchers(HttpMethod.PATCH, API_V1_NEWS_WILDCARD)
            .hasAnyAuthority(AUTHORITY_REPORTER, AUTHORITY_EDITOR)
            .requestMatchers(HttpMethod.DELETE, API_V1_NEWS_WILDCARD).hasAnyAuthority(AUTHORITY_EDITOR)
            .anyRequest().permitAll())
        .formLogin(form -> form
            .successHandler((request, response, authentication) -> apiSecurityService
                .onAuthenticationSuccessForm(jwtTokenService, response, authentication)))
        .csrf(config -> config
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .sessionManagement(config -> config
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(config -> config
            .opaqueToken(
                config2 -> config2.introspector(apiSecurityService::authenticateRequest)
            )
        )
        .oauth2Login(config -> config
            .successHandler((request, response, authentication) -> apiSecurityService
                .onAuthenticationSuccessOauth(jwtTokenService, response, authentication))
        );
    return http.build();
  }


  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    try {
      return config.getAuthenticationManager();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to build AuthenticationManager", e);
    }
  }
}
