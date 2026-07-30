package com.redmath.training.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.training.config.RagProperties;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;

@ExtendWith(MockitoExtension.class)
class RagChatServiceImplTest {

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ChatClientRequestSpec requestSpec;

  @Mock
  private CallResponseSpec responseSpec;

  @Mock
  private VectorStore vectorStore;

  @Mock
  private ChatMemory chatMemory;

  @Test
  void answersQuestionThroughConfiguredChatClient() {
    when(chatClientBuilder.build()).thenReturn(chatClient);
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.advisors(any(Advisor[].class))).thenReturn(requestSpec);
    when(requestSpec.advisors(Mockito.<Consumer<ChatClient.AdvisorSpec>>any())).thenReturn(requestSpec);
    when(requestSpec.user("What is RAG?")).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn("Retrieval augmented generation");

    RagChatServiceImpl service = new RagChatServiceImpl(
        chatClientBuilder,
        vectorStore,
        chatMemory,
        new RagProperties(128, 0.7, 4, null)
    );

    String answer = service.answer("What is RAG?", "conversation-1");

    assertThat(answer).isEqualTo("Retrieval augmented generation");
    verify(requestSpec).user("What is RAG?");
  }
}
