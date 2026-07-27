package com.redmath.training.news.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleValidationExceptions_shouldReturnFieldErrors() {
    org.springframework.validation.FieldError fieldError = new org.springframework.validation.FieldError(
        "newsDto", "title", "Title cannot be empty");
    org.springframework.validation.BindingResult bindingResult = org.mockito.Mockito.mock(
        org.springframework.validation.BindingResult.class);
    org.mockito.Mockito.when(bindingResult.getFieldErrors())
        .thenReturn(java.util.List.of(fieldError));
    MethodArgumentNotValidException ex = org.mockito.Mockito.mock(
        MethodArgumentNotValidException.class);
    org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);

    Map<String, String> result = handler.handleValidationExceptions(ex);

    assertThat(result).hasSize(1);
    assertThat(result.get("title")).isEqualTo("Title cannot be empty");
  }

  @Test
  void handleNotFound_shouldReturnNotFoundStatus() {
    java.util.NoSuchElementException ex = new java.util.NoSuchElementException("News not found: 1");

    Map<String, String> result = handler.handleNotFound(ex);

    assertThat(result).hasSize(1);
    assertThat(result.get("message")).isEqualTo("News not found: 1");
  }

  @Test
  void handleAccessDenied_shouldReturnForbiddenStatus() {
    AccessDeniedException ex = new AccessDeniedException("Access denied");

    Map<String, String> result = handler.handleAccessDenied(ex);

    assertThat(result).hasSize(1);
    assertThat(result.get("message")).isEqualTo("Access denied");
  }

  @Test
  void handleAuthenticationFailure_shouldReturnUnauthorizedStatus() {
    AuthenticationException ex = new org.springframework.security.authentication.BadCredentialsException(
        "Bad credentials");

    Map<String, String> result = handler.handleAuthenticationFailure(ex);

    assertThat(result).hasSize(1);
    assertThat(result.get("message")).isEqualTo("Bad credentials");
  }
}
