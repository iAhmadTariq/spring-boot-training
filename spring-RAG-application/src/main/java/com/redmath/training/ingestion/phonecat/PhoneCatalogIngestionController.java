package com.redmath.training.ingestion.phonecat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingest/phonecat")
public class PhoneCatalogIngestionController {

  private final PhoneCatalogIngestionService ingestionService;

  public PhoneCatalogIngestionController(PhoneCatalogIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping
  public ResponseEntity<String> ingest() {
    int count = ingestionService.ingestAll();
    return ResponseEntity.ok("Ingested %d documents".formatted(count));
  }
}