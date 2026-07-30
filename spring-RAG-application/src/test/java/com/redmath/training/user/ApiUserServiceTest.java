package com.redmath.training.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class ApiUserServiceTest {

  @Mock
  private ApiUserRepository repository;

  @Test
  void loadsUserDetailsByUsername() {
    ApiUserService service = new ApiUserService(repository);
    ApiUser user = user("alice", "secret", "ROLE_USER,ROLE_ADMIN");
    when(repository.findByUserName("alice")).thenReturn(Optional.of(user));

    var details = service.loadUserByUsername("alice");

    assertThat(details.getUsername()).isEqualTo("alice");
    assertThat(details.getPassword()).isEqualTo("secret");
    assertThat(details.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
  }

  @Test
  void throwsWhenUsernameDoesNotExist() {
    ApiUserService service = new ApiUserService(repository);
    when(repository.findByUserName("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("missing"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("Username doesn't exist");
  }

  @Test
  void generatesAndPersistsToken() {
    ApiUserService service = new ApiUserService(repository);
    ApiUser user = user("alice", "secret", "ROLE_USER");
    when(repository.findByUserName("alice")).thenReturn(Optional.of(user));
    when(repository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

    ApiUser updated = service.generateToken("alice");

    assertThat(updated.getToken()).isNotBlank();
    assertThat(updated.getToken()).isEqualTo(user.getToken());
  }

  @Test
  void findsUserByToken() {
    ApiUserService service = new ApiUserService(repository);
    ApiUser user = user("alice", "secret", "ROLE_USER");
    when(repository.findByToken("token-1")).thenReturn(Optional.of(user));

    assertThat(service.findByToken("token-1")).isSameAs(user);
  }

  @Test
  void throwsWhenTokenIsInvalid() {
    ApiUserService service = new ApiUserService(repository);
    when(repository.findByToken("bad-token")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findByToken("bad-token"))
        .isInstanceOf(OAuth2AuthenticationException.class)
        .hasMessage("Invalid or expired access token");
  }

  private ApiUser user(String username, String password, String roles) {
    ApiUser user = new ApiUser();
    user.setUserName(username);
    user.setPassword(password);
    user.setRoles(roles);
    return user;
  }
}
