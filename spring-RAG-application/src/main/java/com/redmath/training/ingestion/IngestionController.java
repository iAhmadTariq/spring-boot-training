package com.redmath.training.ingestion;


import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestionController {

  private final DocumentIngestionService ingestionService;

  public IngestionController(DocumentIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping
  public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file) {
    Resource resource;
    try {
      resource = new InputStreamResource(file.getInputStream()) {
        @Override
        public String getFilename() {
          return file.getOriginalFilename();
        }
      };
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded file", e);
    }

    int chunkCount = ingestionService.ingest(resource);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Ingested %d chunks from %s".formatted(chunkCount, file.getOriginalFilename()));
  }

}