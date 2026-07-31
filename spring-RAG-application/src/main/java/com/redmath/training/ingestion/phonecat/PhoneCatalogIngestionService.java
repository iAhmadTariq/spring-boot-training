package com.redmath.training.ingestion.phonecat;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class PhoneCatalogIngestionService {

  private static final Logger log = LoggerFactory.getLogger(PhoneCatalogIngestionService.class);

  private static final String BASE_URL =
      "https://raw.githubusercontent.com/angular/angular-phonecat/master/app/phones";
  private static final String SUMMARY_URL = BASE_URL + "/phones.json";

  private final VectorStore vectorStore;
  private final PhoneIdExtractor phoneIdExtractor;

  public PhoneCatalogIngestionService(VectorStore vectorStore, PhoneIdExtractor phoneIdExtractor) {
    this.vectorStore = vectorStore;
    this.phoneIdExtractor = phoneIdExtractor;
  }

  public int ingestAll() {
    Resource summaryResource = toResource(SUMMARY_URL);

    List<Document> documents = new ArrayList<>(readJson(summaryResource, "phones.json"));

    for (String phoneId : phoneIdExtractor.extractIds(summaryResource)) {
      Resource detailResource = toResource(BASE_URL + "/" + phoneId + ".json");
      documents.addAll(readJson(detailResource, phoneId + ".json"));
    }

//    vectorStore.add(documents);
    log.info("Ingested {} documents from the phone catalog", documents.size());

    return documents.size();
  }

  private List<Document> readJson(Resource resource, String sourceName) {
    try {
      JsonReader jsonReader = new JsonReader(resource);
      return jsonReader.get();
    } catch (Exception e) {
      log.warn("Skipping '{}' due to read failure: {}", sourceName, e.getMessage());
      return List.of();
    }
  }

  private Resource toResource(String url) {
    try {
      return new UrlResource(url);
    } catch (MalformedURLException e) {
      throw new PhoneCatalogIngestionException("Invalid resource URL: " + url, e);
    }
  }
}