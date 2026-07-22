package com.redmath.training.welcome;

import com.redmath.training.config.AppMessageProperties;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/welcome")
public class WelcomeController {

  private final AppMessageProperties appMessageProperties;

  public WelcomeController(AppMessageProperties appMessageProperties) {
    this.appMessageProperties = appMessageProperties;
  }

  @GetMapping
  public Map<String, Object> welcome() {
    return Map.of("message", appMessageProperties.getWelcome(), "at", LocalDateTime.now());
  }
}
