package com.redmath.training.ingestion.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionControllerTest {

  @Mock
  private DocumentIngestionService ingestionService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new DocumentIngestionController(ingestionService))
        .build();
  }

  @Test
  void returnsCreatedResponseWithChunkCount() throws Exception {
    when(ingestionService.ingest(any())).thenReturn(3);

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "notes.txt",
        "text/plain",
        "spring ai".getBytes()
    );

    mockMvc.perform(multipart("/api/v1/ingest/document").file(file))
        .andExpect(status().isCreated())
        .andExpect(content().string("Ingested 3 chunks from notes.txt"));

    ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(ingestionService).ingest(resourceCaptor.capture());
    assertThat(resourceCaptor.getValue().getFilename()).isEqualTo("notes.txt");
  }
}
