package com.redmath.training.ingestion.phonecat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PhoneCatalogIngestionControllerTest {

  @Mock
  private PhoneCatalogIngestionService ingestionService;

  @Test
  void returnsOkResponseWithDocumentCount() {
    PhoneCatalogIngestionController controller = new PhoneCatalogIngestionController(
        ingestionService);
    when(ingestionService.ingestAll()).thenReturn(11);

    var response = controller.ingest();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("Ingested 11 documents");
  }
}
