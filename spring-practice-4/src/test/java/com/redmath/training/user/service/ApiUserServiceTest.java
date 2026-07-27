package com.redmath.training.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.repository.ApiUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@EnableCaching
class ApiUserServiceTest {

  @Mock
  private ApiUserRepository repository;

  private ApiUserService apiUserService;
  private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    apiUserService = new ApiUserService(repository);
    cacheManager = new ConcurrentMapCacheManager("ApiUser");
  }

  @Test
  void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setPassword("{noop}password");
    user.setRoles("reporter,editor");

    when(repository.findByUserName("testuser")).thenReturn(Optional.of(user));

    UserDetails result = apiUserService.loadUserByUsername("testuser");

    assertThat(result.getUsername()).isEqualTo("testuser");
    assertThat(result.getPassword()).isEqualTo("{noop}password");
    assertThat(result.getAuthorities()).hasSize(2);
  }

  @Test
  void loadUserByUsername_shouldThrow_whenUserDoesNotExist() {
    when(repository.findByUserName("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> apiUserService.loadUserByUsername("nonexistent"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Username doesn't exist");
  }

  @Test
  void loadUserByUsername_shouldThrow_whenRolesAreNull() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setPassword("{noop}password");
    user.setRoles(null);

    when(repository.findByUserName("testuser")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> apiUserService.loadUserByUsername("testuser"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User has no roles assigned");
  }

  @Test
  void loadUserByUsername_shouldThrow_whenRolesAreBlank() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setPassword("{noop}password");
    user.setRoles("   ");

    when(repository.findByUserName("testuser")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> apiUserService.loadUserByUsername("testuser"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User has no roles assigned");
  }

  @Test
  void generateToken_shouldSetTokenAndSave() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setPassword("{noop}password");
    user.setRoles("reporter");

    when(repository.findByUserName("testuser")).thenReturn(Optional.of(user));
    when(repository.save(any(ApiUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ApiUser result = apiUserService.generateToken("testuser");

    assertThat(result.getToken()).isNotNull();
    verify(repository).save(user);
  }

  @Test
  void generateToken_shouldThrow_whenUserNotFound() {
    when(repository.findByUserName("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> apiUserService.generateToken("nonexistent"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("No API user found");
  }

  @Test
  void findByToken_shouldReturnUser_whenTokenExists() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setToken("valid-token");

    when(repository.findByToken("valid-token")).thenReturn(Optional.of(user));

    ApiUser result = apiUserService.findByToken("valid-token");

    assertThat(result).isEqualTo(user);
  }

  @Test
  void provisionOauth2User_shouldReturnExistingUser_whenUserExists() {
    ApiUser existingUser = new ApiUser();
    existingUser.setUserName("oauthuser");
    existingUser.setRoles("reporter");

    when(repository.findByUserName("oauthuser")).thenReturn(Optional.of(existingUser));

    ApiUser result = apiUserService.provisionOauth2User("oauthuser");

    assertThat(result).isEqualTo(existingUser);
    verify(repository, never()).save(any(ApiUser.class));
  }

  @Test
  void provisionOauth2User_shouldCreateNewUser_whenUserDoesNotExist() {
    when(repository.findByUserName("newuser")).thenReturn(Optional.empty());
    when(repository.save(any(ApiUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ApiUser result = apiUserService.provisionOauth2User("newuser");

    assertThat(result.getUserName()).isEqualTo("newuser");
    assertThat(result.getRoles()).isEqualTo("reporter");
    assertThat(result.getPassword()).startsWith("{noop}");
    verify(repository).save(any(ApiUser.class));
  }

}
