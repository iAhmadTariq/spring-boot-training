package com.redmath.training.ingestion.phonecat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.UrlResource;

@ExtendWith(MockitoExtension.class)
class PhoneCatalogIngestionServiceTest {

  @Mock
  private VectorStore vectorStore;

  @Mock
  private PhoneIdExtractor phoneIdExtractor;

  @Test
  void ingestsSummaryAndDetailDocuments() {
    PhoneCatalogIngestionService service = new PhoneCatalogIngestionService(vectorStore, phoneIdExtractor);
    java.util.concurrent.atomic.AtomicInteger readerCount = new java.util.concurrent.atomic.AtomicInteger();

    try (MockedConstruction<UrlResource> ignoredUrls = org.mockito.Mockito.mockConstruction(UrlResource.class);
         MockedConstruction<JsonReader> ignoredReaders = org.mockito.Mockito.mockConstruction(JsonReader.class,
             (reader, context) -> {
               context.arguments();
               int index = readerCount.getAndIncrement();
               when(reader.get()).thenReturn(index == 0
                   ? java.util.List.of(new Document("summary-doc"))
                   : java.util.List.of(new Document("detail-doc")));
             })) {
      UrlResource summaryResource = new UrlResource(URI.create("https://example.com/phones.json"));
      when(phoneIdExtractor.extractIds(any())).thenReturn(java.util.List.of("alpha", "beta"));

      int count = service.ingestAll();

      assertThat(count).isEqualTo(3);
      verify(vectorStore).add(org.mockito.ArgumentMatchers.argThat(documents ->
          documents.size() == 3
              && "summary-doc".equals(documents.getFirst().getText())
              && "detail-doc".equals(documents.get(1).getText())
              && "detail-doc".equals(documents.get(2).getText())));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void skipsDetailDocumentsWhenJsonReadFails() {
    PhoneCatalogIngestionService service = new PhoneCatalogIngestionService(vectorStore, phoneIdExtractor);
    java.util.concurrent.atomic.AtomicInteger readerCount = new java.util.concurrent.atomic.AtomicInteger();

    try (MockedConstruction<UrlResource> ignoredUrls = org.mockito.Mockito.mockConstruction(UrlResource.class);
         MockedConstruction<JsonReader> ignoredReaders = org.mockito.Mockito.mockConstruction(JsonReader.class,
             (reader, context) -> {
               context.arguments();
               int index = readerCount.getAndIncrement();
               if (index == 0) {
                 when(reader.get()).thenReturn(java.util.List.of(new Document("summary-doc")));
               } else {
                 when(reader.get()).thenThrow(new RuntimeException("boom"));
               }
             })) {
      UrlResource summaryResource = new UrlResource(URI.create("https://example.com/phones.json"));
      when(phoneIdExtractor.extractIds(any())).thenReturn(java.util.List.of("alpha"));

      int count = service.ingestAll();

      assertThat(count).isEqualTo(1);
      verify(vectorStore).add(org.mockito.ArgumentMatchers.argThat(documents ->
          documents.size() == 1 && "summary-doc".equals(documents.getFirst().getText())));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void rejectsMalformedResourceUrl() {
    PhoneCatalogIngestionService service = new PhoneCatalogIngestionService(vectorStore, phoneIdExtractor);

    assertThatThrownBy(() -> {
      var method = PhoneCatalogIngestionService.class.getDeclaredMethod("toResource", String.class);
      method.setAccessible(true);
      try {
        method.invoke(service, "ht!tp://example.com");
      } catch (java.lang.reflect.InvocationTargetException e) {
        throw e.getCause();
      }
    })
        .isInstanceOf(PhoneCatalogIngestionException.class)
        .hasMessageContaining("Invalid resource URL");
  }

  @Test
  void createsResourceFromValidUrl() {
    PhoneCatalogIngestionService service = new PhoneCatalogIngestionService(vectorStore, phoneIdExtractor);

    assertThatCode(() -> {
      var method = PhoneCatalogIngestionService.class.getDeclaredMethod("toResource", String.class);
      method.setAccessible(true);
      method.invoke(service, "https://example.com/phones.json");
    })
        .doesNotThrowAnyException();
  }
}
