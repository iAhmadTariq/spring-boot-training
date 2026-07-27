package com.redmath.training.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.redmath.training.user.model.ApiUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ApiUserRepositoryTest {

  @Mock
  private ApiUserRepository apiUserRepository;

  @Test
  void findByUserName_shouldReturnUser_whenUserExists() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setPassword("{noop}password");
    user.setRoles("reporter");

    org.mockito.Mockito.when(apiUserRepository.findByUserName("testuser"))
        .thenReturn(Optional.of(user));

    var result = apiUserRepository.findByUserName("testuser");

    assertThat(result).isPresent();
    assertThat(result.get().getUserName()).isEqualTo("testuser");
  }

  @Test
  void findByUserName_shouldReturnEmpty_whenUserDoesNotExist() {
    org.mockito.Mockito.when(apiUserRepository.findByUserName("nonexistent"))
        .thenReturn(Optional.empty());

    var result = apiUserRepository.findByUserName("nonexistent");

    assertThat(result).isEmpty();
  }

  @Test
  void findByToken_shouldReturnUser_whenTokenExists() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    user.setToken("valid-token");

    org.mockito.Mockito.when(apiUserRepository.findByToken("valid-token"))
        .thenReturn(Optional.of(user));

    var result = apiUserRepository.findByToken("valid-token");

    assertThat(result).isPresent();
    assertThat(result.get().getToken()).isEqualTo("valid-token");
  }

  @Test
  void findByToken_shouldReturnEmpty_whenTokenDoesNotExist() {
    org.mockito.Mockito.when(apiUserRepository.findByToken("nonexistent-token"))
        .thenReturn(Optional.empty());

    var result = apiUserRepository.findByToken("nonexistent-token");

    assertThat(result).isEmpty();
  }

  @Test
  void save_shouldPersistUser() {
    ApiUser user = new ApiUser();
    user.setUserName("newuser");
    user.setPassword("{noop}password");
    user.setRoles("reporter");

    org.mockito.Mockito.when(apiUserRepository.save(user)).thenReturn(user);

    ApiUser saved = apiUserRepository.save(user);

    assertThat(saved.getUserName()).isEqualTo("newuser");
  }

  @Test
  void findAll_shouldReturnAllUsers() {
    ApiUser user1 = new ApiUser();
    user1.setUserName("user1");
    user1.setRoles("reporter");

    ApiUser user2 = new ApiUser();
    user2.setUserName("user2");
    user2.setRoles("editor");

    org.mockito.Mockito.when(apiUserRepository.findAll()).thenReturn(List.of(user1, user2));

    List<ApiUser> result = apiUserRepository.findAll();

    assertThat(result).hasSize(2);
  }

  @Test
  void findAll_shouldReturnPagedResults() {
    ApiUser user = new ApiUser();
    user.setUserName("testuser");
    Page<ApiUser> page = new PageImpl<>(List.of(user), Pageable.unpaged(), 1);

    org.mockito.Mockito.when(
        apiUserRepository.findAll(org.mockito.ArgumentMatchers.<Pageable>any())).thenReturn(page);

    Page<ApiUser> result = apiUserRepository.findAll(Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }
}
