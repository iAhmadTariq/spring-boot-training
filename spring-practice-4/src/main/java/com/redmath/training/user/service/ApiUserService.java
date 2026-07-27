package com.redmath.training.user.service;

import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.repository.ApiUserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class ApiUserService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(ApiUserService.class);
  private static final String ROLE_REPORTER = "reporter";
  private final ApiUserRepository repository;

  ApiUserService(ApiUserRepository repository) {
    this.repository = repository;
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ApiUser user = repository.findByUserName(username)
        .orElseThrow(() -> new UsernameNotFoundException("Username doesn't exist: " + username));

    String roles = user.getRoles();
    if (roles == null || roles.isBlank()) {
      throw new UsernameNotFoundException("User has no roles assigned: " + username);
    }

    return User.withUsername(username).password(user.getPassword())
        .authorities(roles.split(",")).build();
  }

  public ApiUser generateToken(String username) {
    ApiUser user = repository.findByUserName(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("No API user found for username: " + username));

    user.setToken(UUID.randomUUID().toString());
    return repository.save(user);
  }

  @Cacheable(cacheNames = "ApiUser")
  public ApiUser findByToken(String token) {
    return repository.findByToken(token)
        .orElseThrow(() -> new OAuth2AuthenticationException("Invalid or expired access token"));
  }

  public ApiUser provisionOauth2User(String username) {
    return repository.findByUserName(username)
        .orElseGet(() -> {
          ApiUser newUser = new ApiUser();
          newUser.setUserName(username);
          newUser.setPassword("{noop}" + UUID.randomUUID());
          newUser.setRoles(ROLE_REPORTER);
          log.info("Provisioned new OAuth2 user: {}", username);
          return repository.save(newUser);
        });
  }

  @CacheEvict(cacheNames = "ApiUser", allEntries = true)
  @Scheduled(fixedDelay = 60000)
  public void clearExpiredTokens() {
    int clearedCount = 0;
    for (ApiUser user : repository.findAll()) {
      if (user.getToken() != null) {
        user.setToken(null);
        repository.save(user);
        clearedCount++;
      }
    }
    if (clearedCount > 0) {
      log.info("Cleared {} expired tokens", clearedCount);
    }
  }

}
