package com.training.redmath.welcome;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/welcome")
public class WelcomeController {


  @GetMapping
  public Map<String, String> welcome() {
    Map<String, String> response = new HashMap<>();
    response.put("message", "Welcome to RAG Application");
    return response;
  }
}
