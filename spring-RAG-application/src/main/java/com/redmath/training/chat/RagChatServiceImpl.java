package com.redmath.training.chat;

import com.redmath.training.config.RagProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagChatServiceImpl implements RagChatService {

  private static final String CUSTOM_QA_TEMPLATE = """
      You must answer ONLY using the information in the CONTEXT below.
      Use the conversation history only to understand follow-up questions
      (e.g. pronouns like "it" or "that phone"), not as a source of facts.
      
      Rules:
      - If the answer is not explicitly present in the CONTEXT, respond exactly: "I don't have information about that in the provided document."
      - Do NOT use any outside knowledge, even if you know the answer.
      - Do NOT guess or infer beyond what the CONTEXT states.
      
      CONTEXT:
      {question_answer_context}
      
      QUESTION:
      {query}
      
      ANSWER (from CONTEXT only):
      """;

  private final ChatClient chatClient;
  private final QuestionAnswerAdvisor questionAnswerAdvisor;
  private final MessageChatMemoryAdvisor chatMemoryAdvisor;

  public RagChatServiceImpl(ChatClient.Builder chatClientBuilder,
      VectorStore vectorStore,
      ChatMemory chatMemory,
      RagProperties properties) {
    this.chatClient = chatClientBuilder.build();

    this.questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder()
            .similarityThreshold(properties.similarityThreshold())
            .topK(properties.topK())
            .build())
        .promptTemplate(PromptTemplate.builder().template(CUSTOM_QA_TEMPLATE).build())
        .build();

    this.chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
  }

  @Override
  public String answer(String question, String conversationId) {
    return chatClient.prompt()
        .advisors(chatMemoryAdvisor, questionAnswerAdvisor)
        .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
        .advisors(new SimpleLoggerAdvisor())
        .user(question)
        .call()
        .content();
  }
}