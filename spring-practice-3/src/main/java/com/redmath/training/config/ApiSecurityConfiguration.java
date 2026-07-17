package com.redmath.training.config;

import com.redmath.training.security.AccessTokenCookieIssuer;
import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.service.ApiUserService;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiUserService userService,
                                           AccessTokenCookieIssuer accessTokenCookieIssuer,
                                           ApiUserService apiUserService){
        http.authorizeHttpRequests(config-> config
                .requestMatchers("/api/v1/welcome","/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/news","/api/v1/news/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/news").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/news/*").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasAnyRole("editor")
                .anyRequest().hasRole("admin"))
                .formLogin(form-> form.successHandler(((request, response, authentication) -> {
                    accessTokenCookieIssuer.issueFor(authentication.getName(),response);
                })))
                .csrf(config->config.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(handling -> handling.accessDeniedHandler(accessDeniedHandler()))
                .oauth2ResourceServer(config -> config.opaqueToken(
                    config2 -> config2.introspector(token -> {
                        ApiUser user = apiUserService.findByToken(token);
                        String[] roles = Arrays.stream(user.getRoles().split(","))
                                .map(role -> "ROLE_" + role.trim())
                                .toArray(String[]::new);
                        return new DefaultOAuth2AuthenticatedPrincipal(
                                user.getUserName(),
                                Map.of("sub",user.getUserName()),
                                AuthorityUtils.createAuthorityList(roles)
                        );
                    })
                ))
                .oauth2Login(config-> config.successHandler((
                        (request, response, authentication) -> {
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oAuth2User.getAttribute("email");
                            apiUserService.provisionOAuth2User(email);
                            accessTokenCookieIssuer.issueFor(email,response);
                        })
                ));

        return http.build();
    }

    private AccessDeniedHandler accessDeniedHandler(){
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String message = ex.getMessage().replace("\\", "\\\\").replace("\"", "\\\"");
            response.getWriter().write("{\"message\":\"" + message + "\"}");
        };
    }
}
