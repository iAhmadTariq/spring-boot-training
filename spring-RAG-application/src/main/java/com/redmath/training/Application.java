package com.redmath.training;

import com.redmath.training.config.RagProperties;
import java.util.Locale;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class Application {

  static {
    init();
  }

  public static void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale.setDefault(Locale.US);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
