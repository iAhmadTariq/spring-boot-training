package com.redmath.training.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class JwtConfigTest {

  @Test
  void rsaKeyPair_shouldGenerateValidKeyPair() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(JwtConfig.class);
    context.refresh();

    KeyPair keyPair = context.getBean(KeyPair.class);

    assertThat(keyPair).isNotNull();
    assertThat(keyPair.getPublic()).isNotNull();
    assertThat(keyPair.getPrivate()).isNotNull();
    assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");

    context.close();
  }

  @Test
  void jwtEncoder_shouldBeCreated() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(JwtConfig.class);
    context.refresh();

    var encoder = context.getBean(org.springframework.security.oauth2.jwt.JwtEncoder.class);

    assertThat(encoder).isNotNull();

    context.close();
  }

  @Test
  void jwtDecoder_shouldBeCreated() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(JwtConfig.class);
    context.refresh();

    var decoder = context.getBean(org.springframework.security.oauth2.jwt.JwtDecoder.class);

    assertThat(decoder).isNotNull();

    context.close();
  }

  @Test
  void keyPair_shouldBeUniquePerBean() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(JwtConfig.class);
    context.refresh();

    KeyPair keyPair1 = context.getBean(KeyPair.class);
    KeyPair keyPair2 = context.getBean(KeyPair.class);

    assertThat(keyPair1).isSameAs(keyPair2);

    context.close();
  }
}
