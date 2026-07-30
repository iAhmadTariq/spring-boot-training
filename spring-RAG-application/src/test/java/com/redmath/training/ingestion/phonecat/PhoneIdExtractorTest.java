package com.redmath.training.ingestion.phonecat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import tools.jackson.databind.ObjectMapper;

class PhoneIdExtractorTest {

  private final PhoneIdExtractor extractor = new PhoneIdExtractor(new ObjectMapper());

  @Test
  void extractsIdsFromPhoneSummary() {
    var resource = new ByteArrayResource("""
        [
          {"id":"nexus-s"},
          {"id":"pixel-5"}
        ]
        """.getBytes(StandardCharsets.UTF_8));

    List<String> ids = extractor.extractIds(resource);

    assertThat(ids).containsExactly("nexus-s", "pixel-5");
  }

  @Test
  void wrapsReadFailures() {
    var resource = new ByteArrayResource("not json".getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> extractor.extractIds(resource))
        .isInstanceOf(PhoneCatalogIngestionException.class)
        .hasMessageContaining("Failed to read phone ids from phones.json");
  }
}
