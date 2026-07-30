package com.redmath.training.chat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redmath.training.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RagChatControllerTest {

  @Mock
  private RagChatService ragChatService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new RagChatController(ragChatService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void returnsAnswerFromService() throws Exception {
    when(ragChatService.answer("What is RAG?", "conversation-1"))
        .thenReturn("Retrieval augmented generation");

    mockMvc.perform(post("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"question":"What is RAG?","conversationId":"conversation-1"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value("Retrieval augmented generation"));

    verify(ragChatService).answer(eq("What is RAG?"), eq("conversation-1"));
  }

  @Test
  void rejectsBlankPayloadFields() throws Exception {
    mockMvc.perform(post("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"question":"","conversationId":""}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid request payload"));
  }
}
