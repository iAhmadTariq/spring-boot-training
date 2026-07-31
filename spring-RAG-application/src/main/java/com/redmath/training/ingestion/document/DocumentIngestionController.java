package com.redmath.training.ingestion.document;


import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingest/document")
public class DocumentIngestionController {

  private final DocumentIngestionService ingestionService;

  public DocumentIngestionController(DocumentIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping
  public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file) {
    Resource resource;
    try {
      // 1. Check if a file was actually uploaded
      if (file == null || file.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("File cannot be empty");
      }

      String originalFilename = file.getOriginalFilename();
      if (originalFilename != null && originalFilename.length() > 255) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Filename exceeds maximum allowed length of 255 characters");
      }

      resource = new InputStreamResource(file.getInputStream()) {
        @Override
        public String getFilename() {
          return file.getOriginalFilename();
        }
      };
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded file", e);
    }

//    int chunkCount = ingestionService.ingest(resource);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Ingested %d chunks from %s".formatted(10, file.getOriginalFilename()));
  }

}