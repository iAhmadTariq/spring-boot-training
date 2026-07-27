package com.training.redmath.chat;

import com.training.redmath.config.RagProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagChatServiceImpl implements RagChatService {

  private final ChatClient chatClient;
  private final QuestionAnswerAdvisor questionAnswerAdvisor;

  public RagChatServiceImpl(ChatClient.Builder chatClientBuilder,
      VectorStore vectorStore,
      RagProperties properties) {
    this.chatClient = chatClientBuilder.build();
    this.questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder()
            .similarityThreshold(properties.similarityThreshold())
            .topK(properties.topK())
            .build())
        .build();
  }

  @Override
  public String answer(String question) {
    return chatClient.prompt()
        .advisors(questionAnswerAdvisor)
        .user(question)
        .call()
        .content();
  }
}
