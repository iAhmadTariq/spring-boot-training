package com.redmath.training.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

  private final SignatureAlgorithm jwtAlgorithm;

  public JwtConfig() {
    this.jwtAlgorithm = SignatureAlgorithm.PS256;
  }

  @Bean
  public KeyPair rsaKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    return keyPairGenerator.generateKeyPair();
  }

  @Bean
  public JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
    return NimbusJwtEncoder.withKeyPair((RSAPublicKey) rsaKeyPair.getPublic(),
            (RSAPrivateKey) rsaKeyPair.getPrivate())
        .algorithm(jwtAlgorithm).build();
  }

  @Bean
  public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
    return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic())
        .signatureAlgorithm(jwtAlgorithm)
        .build();
  }
}
