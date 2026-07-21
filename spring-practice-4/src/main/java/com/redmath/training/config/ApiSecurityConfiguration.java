package com.redmath.training.config;

import com.redmath.training.security.ApiSecurityService;
import com.redmath.training.security.JwtTokenService;
import com.redmath.training.user.service.ApiUserService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiUserService userService,
                                           JwtTokenService jwtTokenService,
                                           ApiUserService apiUserService,
                                           ApiSecurityService apiSecurityService){
        http.authorizeHttpRequests(config-> config.requestMatchers("/api/v1/auth/*").permitAll()
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/news","/api/v1/news/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/news").hasAnyAuthority("reporter","editor")
                .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyAuthority("reporter","editor")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/news/*").hasAnyAuthority("reporter","editor")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasAnyAuthority("editor")
                .anyRequest().authenticated())
                .formLogin(form-> form.successHandler(((request, response, authentication) -> {
                    apiSecurityService.onAuthenticationSuccessForm(jwtTokenService,request,response,authentication);
                })))
                .csrf(config->config.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(config -> config.opaqueToken(
                    config2 -> config2.introspector(apiSecurityService::authenticateRequest)
                ))
                .oauth2Login(config-> config.successHandler((
                        (request, response, authentication) -> {
                            apiSecurityService.onAuthenticationSuccessOAuth(jwtTokenService,request,response,authentication);
                        })
                ));
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
