package com.redmath.training.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void mapsIllegalArgumentToBadRequest() {
    var response = handler.handleBadRequest(new IllegalArgumentException("bad input"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo(new ApiError("bad input"));
  }

  @Test
  void mapsUncheckedIOExceptionToServerError() {
    var response = handler.handleIoError(
        new UncheckedIOException("failed", new java.io.IOException()));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isEqualTo(new ApiError("Failed to process file"));
  }

  @Test
  void mapsUnexpectedExceptionToServerError() {
    var response = handler.handleUnexpected(new RuntimeException("boom"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isEqualTo(new ApiError("Unexpected error occurred"));
  }
}
