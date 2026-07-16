package com.redmath.training.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http.authorizeHttpRequests(config-> config
                .requestMatchers("/api/v1/welcome","/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/news","/api/v1/news/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/news").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/news/*").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasAnyRole("editor")
                .anyRequest().hasRole("admin"))
                .formLogin(form-> form.defaultSuccessUrl("/",true))
                .csrf(config->config.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
        return http.build();
    }
}
