package com.redmath.training.config;

import java.io.File;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Persists the in-memory vector store to disk when the application context
 * shuts down, so ingested documents survive restarts during development.
 */
@Component
public class VectorStorePersistenceListener {

  private final VectorStore vectorStore;
  private final RagProperties ragProperties;

  public VectorStorePersistenceListener(VectorStore vectorStore, RagProperties ragProperties) {
    this.vectorStore = vectorStore;
    this.ragProperties = ragProperties;
  }

  @EventListener(ContextClosedEvent.class)
  public void onShutdown(ContextClosedEvent event) {
    if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
      simpleVectorStore.save(new File(ragProperties.vectorStorePersistPath()));
    }
  }
}