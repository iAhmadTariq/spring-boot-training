package com.redmath.training.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;

@Service
public class ApiUserService implements UserDetailsService {

  private final ApiUserRepository repository;

  ApiUserService(ApiUserRepository repository) {
    this.repository = repository;
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<ApiUser> user = repository.findByUserName(username);

    if (user.isEmpty()) {
      throw new UsernameNotFoundException("Username doesn't exist");
    }
    return User.withUsername(username).password(user.get().getPassword())
        .authorities(user.get().getRoles().split(",")).build();

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
        .orElseThrow(() -> new OAuth2AuthenticationException(
            new OAuth2Error("invalid_token", "Invalid or expired access token", null)));
  }

}
