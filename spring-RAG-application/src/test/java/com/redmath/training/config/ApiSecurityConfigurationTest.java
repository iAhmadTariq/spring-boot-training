package com.redmath.training.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

class ApiSecurityConfigurationTest {

  @Test
  void returnsAuthenticationManagerFromConfiguration() {
    AuthenticationConfiguration configuration = Mockito.mock(AuthenticationConfiguration.class);
    AuthenticationManager manager = Mockito.mock(AuthenticationManager.class);
    when(configuration.getAuthenticationManager()).thenReturn(manager);

    ApiSecurityConfiguration apiSecurityConfiguration = new ApiSecurityConfiguration();

    assertThat(apiSecurityConfiguration.authenticationManager(configuration)).isSameAs(manager);
  }

  @Test
  void wrapsAuthenticationManagerBuildFailures() {
    AuthenticationConfiguration configuration = Mockito.mock(AuthenticationConfiguration.class);
    when(configuration.getAuthenticationManager()).thenThrow(new IllegalStateException("boom"));

    ApiSecurityConfiguration apiSecurityConfiguration = new ApiSecurityConfiguration();

    assertThatThrownBy(() -> apiSecurityConfiguration.authenticationManager(configuration))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to build AuthenticationManager")
        .hasCauseInstanceOf(IllegalStateException.class);
  }
}
