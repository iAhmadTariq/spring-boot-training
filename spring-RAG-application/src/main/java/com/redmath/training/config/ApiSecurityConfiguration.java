package com.redmath.training.config;

import com.redmath.training.security.ApiSecurityService;
import com.redmath.training.security.JwtTokenService;
import com.redmath.training.user.ApiUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, ApiUserService userService,
      JwtTokenService jwtTokenService,
      ApiUserService apiUserService,
      ApiSecurityService apiSecurityService) {
    http.authorizeHttpRequests(config -> config
            .requestMatchers("/api/v1/**").authenticated()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
            .permitAll())
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
