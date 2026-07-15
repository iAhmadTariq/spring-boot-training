package com.redmath.training.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ApiSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http.authorizeHttpRequests(config-> config
                .requestMatchers("/api/v1/welcome","/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/news","/api/v1/news/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/news").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.PUT, "/api/v1/news/*").hasAnyRole("editor")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/news/*").hasAnyRole("editor")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*").hasAnyRole("editor")
                .anyRequest().hasRole("admin"))
                .formLogin(form-> form.defaultSuccessUrl("/",true))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager(){
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("admin","editor","reporter")
                .build();

        UserDetails reporter = User.withUsername("reporter")
                .password(encoder.encode("reporter123"))
                .roles("reporter")
                .build();

        UserDetails editor = User.withUsername(("editor"))
                .password(encoder.encode("editor123"))
                .roles("editor")
                .build();

        return new InMemoryUserDetailsManager(admin,reporter,editor);
    }
}
