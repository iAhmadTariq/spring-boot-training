package com.redmath.training.ingestion.phonecat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class PhoneIdExtractor {

  private final ObjectMapper objectMapper;

  public PhoneIdExtractor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<String> extractIds(Resource phonesSummaryResource) {
    try {
      JsonNode root = objectMapper.readTree(phonesSummaryResource.getInputStream());
      List<String> ids = new ArrayList<>();
      root.forEach(node -> ids.add(node.path("id").asString()));
      return ids;
    } catch (IOException e) {
      throw new PhoneCatalogIngestionException("Failed to read phone ids from phones.json", e);
    }
  }
}