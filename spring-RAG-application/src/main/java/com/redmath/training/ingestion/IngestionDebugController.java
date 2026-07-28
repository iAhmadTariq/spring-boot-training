package com.redmath.training.ingestion;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestionDebugController {

  private final VectorStore vectorStore;

  public IngestionDebugController(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  @GetMapping("/api/v1/ingest/debug-search")
  public List<String> debugSearch(@RequestParam String query) {
    return vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(3).build())
        .stream()
        .map(Document::getText)
        .toList();
  }
}