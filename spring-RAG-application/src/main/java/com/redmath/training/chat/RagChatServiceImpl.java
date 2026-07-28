package com.redmath.training.chat;

import com.redmath.training.config.RagProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagChatServiceImpl implements RagChatService {

  private static final String CUSTOM_QA_TEMPLATE = """
        You must answer ONLY using the information in the CONTEXT below.
        
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

  public RagChatServiceImpl(ChatClient.Builder chatClientBuilder,
      VectorStore vectorStore,
      RagProperties properties) {
    this.chatClient = chatClientBuilder.build();
    this.questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder()
            .similarityThreshold(properties.similarityThreshold())
            .topK(properties.topK())
            .build()).promptTemplate(PromptTemplate.builder().template(CUSTOM_QA_TEMPLATE).build())
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
