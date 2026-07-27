package com.training.redmath.config;

import java.io.File;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

  private final RagProperties ragProperties;

  public VectorStoreConfig(RagProperties ragProperties) {
    this.ragProperties = ragProperties;
  }

  /**
   * In-memory vector store, optionally restored from disk so embeddings
   * survive application restarts during local development.
   */
  @Bean
  public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

    File persistFile = new File(ragProperties.vectorStorePersistPath());
    if (persistFile.exists()) {
      store.load(persistFile);
    }
    return store;
  }
}