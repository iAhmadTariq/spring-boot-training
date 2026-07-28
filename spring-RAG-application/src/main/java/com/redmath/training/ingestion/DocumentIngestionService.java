package com.redmath.training.ingestion;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentIngestionService {

  private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

  private final VectorStore vectorStore;

  public DocumentIngestionService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  public int ingest(Resource resource) {
    if (!resource.exists()) {
      throw new IllegalArgumentException("Resource does not exist: " + resource.getFilename());
    }

    List<Document> rawDocuments = new TikaDocumentReader(resource).get();

    TokenTextSplitter splitter = new TokenTextSplitter();
    List<Document> chunks = splitter.apply(rawDocuments);

    vectorStore.add(chunks);
    log.info("Ingested '{}' into {} chunks", resource.getFilename(), chunks.size());

    return chunks.size();
  }
}