package com.redmath.training.config;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Persists the in-memory vector store to disk when the application context shuts down, so ingested
 * documents survive restarts during development.
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
    // Persistence on shutdown disabled to avoid filesystem errors when the
    // configured persist directory/file doesn't exist or cannot be created.
    // If persistence is desired, create the directory referenced by
    // ragProperties.vectorStorePersistPath() or re-enable saving with a
    // safe try/catch and directory creation.
  }
}