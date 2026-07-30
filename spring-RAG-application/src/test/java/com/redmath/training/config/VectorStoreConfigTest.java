package com.redmath.training.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

class VectorStoreConfigTest {

  @TempDir
  Path tempDir;

  @Test
  void loadsPersistedStoreWhenFileExists() {
    Path persistFile = tempDir.resolve("vector-store.json");
    EmbeddingModel embeddingModel = Mockito.mock(EmbeddingModel.class);

    SimpleVectorStore persisted = SimpleVectorStore.builder(embeddingModel).build();
    persisted.save(persistFile.toFile());

    VectorStoreConfig config = new VectorStoreConfig(
        new RagProperties(128, 0.7, 4, persistFile.toString()));

    VectorStore store = config.vectorStore(embeddingModel);

    assertThat(store).isInstanceOf(SimpleVectorStore.class);
  }

  @Test
  void buildsStoreWithoutLoadingWhenFileIsMissing() {
    Path missingFile = tempDir.resolve("missing-vector-store.json");
    EmbeddingModel embeddingModel = Mockito.mock(EmbeddingModel.class);

    VectorStoreConfig config = new VectorStoreConfig(
        new RagProperties(128, 0.7, 4, missingFile.toString()));

    VectorStore store = config.vectorStore(embeddingModel);

    assertThat(store).isInstanceOf(SimpleVectorStore.class);
    assertThat(Files.exists(missingFile)).isFalse();
  }
}
