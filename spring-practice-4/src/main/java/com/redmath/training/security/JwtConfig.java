package com.redmath.training.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

  private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
  private static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.PS256;

  @Bean
  public KeyPair rsaKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      log.info("Generated RSA 2048-bit key pair for JWT signing");
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("RSA algorithm not available", e);
    }
  }

  @Bean
  public JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
    return NimbusJwtEncoder.withKeyPair((RSAPublicKey) rsaKeyPair.getPublic(),
            (RSAPrivateKey) rsaKeyPair.getPrivate())
        .algorithm(JWT_ALGORITHM).build();
  }

  @Bean
  public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
    return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic())
        .signatureAlgorithm(JWT_ALGORITHM)
        .build();
  }
}
