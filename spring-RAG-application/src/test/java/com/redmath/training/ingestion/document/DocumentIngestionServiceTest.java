package com.redmath.training.ingestion.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

  @Mock
  private VectorStore vectorStore;

  @TempDir
  Path tempDir;

  @Test
  void ingestsExistingTextFileIntoVectorStore() throws IOException {
    DocumentIngestionService service = new DocumentIngestionService(vectorStore);
    Path file = tempDir.resolve("sample.txt");
    Files.writeString(file, "Spring AI makes document ingestion simple.");

    int chunkCount = service.ingest(new FileSystemResource(file));

    assertThat(chunkCount).isEqualTo(1);
    ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
    verify(vectorStore).add(captor.capture());
    assertThat(captor.getValue()).hasSize(1);
  }

  @Test
  void rejectsMissingResources() {
    DocumentIngestionService service = new DocumentIngestionService(vectorStore);

    assertThatThrownBy(() -> service.ingest(new FileSystemResource(tempDir.resolve("missing.txt"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Resource does not exist");
  }
}
