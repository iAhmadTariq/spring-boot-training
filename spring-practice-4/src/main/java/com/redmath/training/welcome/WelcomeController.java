package com.redmath.training.welcome;

import com.redmath.training.config.AppMessageProperties;
import java.util.HashMap;
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
  public Map<String, String> welcome() {
    Map<String, String> response = new HashMap<>();
    response.put("message",
        appMessageProperties.getWelcome() != null ? appMessageProperties.getWelcome()
            : "Default Welcome");
    return response;
  }
}
